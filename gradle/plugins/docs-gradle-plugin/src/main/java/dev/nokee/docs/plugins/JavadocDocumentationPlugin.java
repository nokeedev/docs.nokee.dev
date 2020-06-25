package dev.nokee.docs.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import dev.nokee.docs.JavadocDependencyLock;
import dev.nokee.docs.tasks.GenerateJavadocClasspathLock;
import lombok.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class JavadocDocumentationPlugin implements Plugin<Project> {

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract DependencyHandler getDependencies();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("java-base");

		val javadocSource = getConfigurations().create("javadocSource", this::asIncoming);
		val javadocClasspathLock = getConfigurations().create("javadocClasspathLock", this::asIncoming);
		val javadocClasspath = getConfigurations().create("javadocClasspath", configuration -> {
			asIncoming(configuration);
			configuration.getDependencies().addAllLater(toJavadocClasspath(javadocClasspathLock));
		});

		val javadocTask = getOrRegisterJavadocTask();
		javadocTask.configure(task -> {
			task.source(javadocSource);
			task.setClasspath(task.getClasspath().plus(javadocClasspath));
		});

		project.getExtensions().getByType(SourceSetContainer.class).matching(it -> it.getName().equals("main")).configureEach(sourceSet -> {
			getTasks().register("generateJavadocClasspathLock", GenerateJavadocClasspathLock.class, task -> {
				task.setGroup("documentation");
				task.getDependencies().addAll(lock(getConfigurations().getByName(sourceSet.getImplementationConfigurationName())));
				task.getDependencies().addAll(lock(getConfigurations().getByName(sourceSet.getApiConfigurationName())));
				task.getDependencies().addAll(lock(getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName())));
				task.getGeneratedJavadocClasspathLock().convention(getLayout().getBuildDirectory().file("tmp/" + task.getName() + "/javadoc-classpath-lock.xml"));
			});
		});
	}

	private Provider<? extends Iterable<JavadocDependencyLock.Dependency>> lock(Configuration configuration) {
		return getProviders().provider(() -> configuration.getDependencies().stream().filter(it -> it instanceof ExternalDependency).map(it -> {
			return new JavadocDependencyLock.Dependency(it.getGroup(), it.getName(), it.getVersion());
		}).collect(Collectors.toList()));
	}

	private void asIncoming(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
	}

	private TaskProvider<Javadoc> getOrRegisterJavadocTask() {
		if (getTasks().getNames().contains("javadoc")) {
			return getTasks().named("javadoc", Javadoc.class);
		}
		return getTasks().register("javadoc", Javadoc.class, task -> {
			task.setGroup("documentation");
		});
	}

	private Provider<? extends Iterable<Dependency>> toJavadocClasspath(Configuration configuration) {
		return getObjects().listProperty(Dependency.class).value(configuration.getIncoming().getFiles().getElements().map(new ToJavadocClasspathTransformer()));
	}

	public class ToJavadocClasspathTransformer implements Transformer<Iterable<Dependency>, Iterable<? extends FileSystemLocation>> {
		@SneakyThrows
		@Override
		public Iterable<Dependency> transform(Iterable<? extends FileSystemLocation> dependencyLocks) {
			val result = new ArrayList<Dependency>();

			XmlMapper xmlMapper = new XmlMapper();
			for (FileSystemLocation dependencyLockFile : dependencyLocks) {
				val dependencyLock = xmlMapper.readValue(dependencyLockFile.getAsFile(), JavadocDependencyLock.class);
				for (val dependency : dependencyLock.getDependencies()) {
					result.add(getDependencies().create(String.format("%s:%s:%s", dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion())));
				}
			}
			return result;
		}
	}
}
