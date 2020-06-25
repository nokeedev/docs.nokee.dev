package dev.nokee.docs.plugins;

import dev.nokee.docs.tasks.ProcessJBakeConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;
import org.jbake.gradle.JBakeExtension;
import org.jbake.gradle.JBakeServeTask;
import org.jbake.gradle.JBakeTask;

import javax.inject.Inject;
import java.util.concurrent.Callable;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

public abstract class JBakeArtifactsPlugin implements Plugin<Project> {
	public static final String ASSETS_CONFIGURATION_NAME = "assets";
	public static final String TEMPLATES_CONFIGURATION_NAME = "templates";
	public static final String CONTENT_CONFIGURATION_NAME = "content";
	public static final String CONFIGURATION_CONFIGURATION_NAME = "configuration";
	public static final String BAKED_CONFIGURATION_NAME = "baked";

	public static final String ASSETS_ELEMENTS_CONFIGURATION_NAME = "assetsElements";
	public static final String TEMPLATES_ELEMENTS_CONFIGURATION_NAME = "templatesElements";
	public static final String CONTENT_ELEMENTS_CONFIGURATION_NAME = "contentElements";
	public static final String CONFIGURATION_ELEMENTS_CONFIGURATION_NAME = "configurationElements";
	public static final String BAKED_ELEMENTS_CONFIGURATION_NAME = "bakedElements";

	static final String JBAKE_ASSETS_USAGE_NAME = "jbake-assets";
	static final String JBAKE_TEMPLATES_USAGE_NAME = "jbake-templates";
	static final String JBAKE_CONTENT_USAGE_NAME = "jbake-content";
	static final String JBAKE_CONFIGURATION_USAGE_NAME = "jbake-properties";
	static final String JBAKE_BAKED_USAGE_NAME = "jbake-baked";

	static final Attribute<String> ARTIFACT_FORMAT = Attribute.of("artifactType", String.class);

	@Getter
	private ExtensionContainer extensions;

	@Getter
	private SoftwareComponentContainer components;

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract DependencyHandler getDependencies();

	@Inject
	protected abstract SoftwareComponentFactory getComponentFactory();

	@Override
	public void apply(Project project) {
		extensions = project.getExtensions();
		components = project.getComponents();

		// Incoming
		val baked = getConfigurations().create(BAKED_CONFIGURATION_NAME, this::configureIncomingBaked);
		val assets = getConfigurations().create(ASSETS_CONFIGURATION_NAME, this::configureIncomingAssets);

		// Transforms incoming archives into directory
		getDependencies().registerTransform(UnzipTransform.class, this::transformIncomingBaked);
		getDependencies().registerTransform(UnzipTransform.class, this::transformIncomingAssets);

		val siteTask = getTasks().register("site", Sync.class, task -> {
			task.setDestinationDir(getLayout().getBuildDirectory().dir("site").get().getAsFile());
			task.from(baked);

			// if jbake is applied, we copy to staging and jbake copy here
			task.from(onlyIfJbakePluginIsNotApplied(project.getPluginManager(), assets));
		});

		project.getPluginManager().withPlugin("org.jbake.site", new WireBakingRule(assets, siteTask));
	}

	private Callable<FileCollection> onlyIfJbakePluginIsNotApplied(PluginManager plugins, FileCollection files) {
		return () -> {
			if (plugins.hasPlugin("org.jbake.site")) {
				return getObjects().fileCollection();
			}
			return files;
		};
	}

	private void configureIncomingAssets(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
		configuration.attributes(attributes -> {
			attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_ASSETS_USAGE_NAME));
			attributes.attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		});
	}

	private void transformIncomingAssets(TransformSpec<TransformParameters.None> variantTransform) {
		variantTransform.getFrom().attribute(ARTIFACT_FORMAT, ZIP_TYPE);
		variantTransform.getFrom().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_ASSETS_USAGE_NAME));
		variantTransform.getTo().attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		variantTransform.getTo().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_ASSETS_USAGE_NAME));
	}

	void configureIncomingConfiguration(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
		configuration.attributes(attributes -> {
			attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_CONFIGURATION_USAGE_NAME));
		});
	}

	void configureIncomingContent(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
		configuration.attributes(attributes -> {
			attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_CONTENT_USAGE_NAME));
			attributes.attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		});
	}

	void transformIncomingContent(TransformSpec<TransformParameters.None> variantTransform) {
		variantTransform.getFrom().attribute(ARTIFACT_FORMAT, ZIP_TYPE);
		variantTransform.getFrom().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_CONTENT_USAGE_NAME));
		variantTransform.getTo().attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		variantTransform.getTo().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_CONTENT_USAGE_NAME));
	}

	void configureIncomingTemplates(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
		configuration.attributes(attributes -> {
			attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_TEMPLATES_USAGE_NAME));
			attributes.attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		});
	}

	void transformIncomingTemplates(TransformSpec<TransformParameters.None> variantTransform) {
		variantTransform.getFrom().attribute(ARTIFACT_FORMAT, ZIP_TYPE);
		variantTransform.getFrom().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_TEMPLATES_USAGE_NAME));
		variantTransform.getTo().attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		variantTransform.getTo().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_TEMPLATES_USAGE_NAME));
	}

	private void configureIncomingBaked(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(true);
		configuration.attributes(attributes -> {
			attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_BAKED_USAGE_NAME));
			attributes.attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		});
	}

	private void transformIncomingBaked(TransformSpec<TransformParameters.None> variantTransform) {
		variantTransform.getFrom().attribute(ARTIFACT_FORMAT, ZIP_TYPE);
		variantTransform.getFrom().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_BAKED_USAGE_NAME));
		variantTransform.getTo().attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
		variantTransform.getTo().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_BAKED_USAGE_NAME));
	}


	@RequiredArgsConstructor
	public class WireBakingRule implements Action<AppliedPlugin> {
		private static final String ZIP_ASSETS_TASK_NAME = "zipAssets";
		private static final String ZIP_TEMPLATES_TASK_NAME = "zipTemplates";
		private static final String ZIP_CONTENT_TASK_NAME = "zipContent";
		private static final String ZIP_BAKED_TASK_NAME = "zipBaked";

		private final Configuration assets;
		private final TaskProvider<Sync> siteTask;

		@Override
		public void execute(AppliedPlugin appliedPlugin) {
			// Incoming
			val templates = getConfigurations().create(TEMPLATES_CONFIGURATION_NAME, JBakeArtifactsPlugin.this::configureIncomingTemplates);
			val content = getConfigurations().create(CONTENT_CONFIGURATION_NAME, JBakeArtifactsPlugin.this::configureIncomingContent);

			// Transforms incoming archives into directory
			getDependencies().registerTransform(UnzipTransform.class, JBakeArtifactsPlugin.this::transformIncomingTemplates);
			getDependencies().registerTransform(UnzipTransform.class, JBakeArtifactsPlugin.this::transformIncomingContent);

			// Introduce stage step: stage -> jbake -> site
			val jbake = getExtensions().getByType(JBakeExtension.class);
			val bakeTask = getTasks().named("bake", JBakeTask.class);
			siteTask.configure(task -> {
				task.from(bakeTask);
			});
			getTasks().named("bakePreview", JBakeServeTask.class, task -> {
				task.setInput(siteTask.get().getDestinationDir());
			});

			// JBake configuration
			val configuration = getConfigurations().create(CONFIGURATION_CONFIGURATION_NAME, JBakeArtifactsPlugin.this::configureIncomingConfiguration);
			val processTask = getTasks().register("processJBakeConfiguration", ProcessJBakeConfiguration.class, task -> {
				task.from(configuration);
				task.from(getLayout().getProjectDirectory().file(jbake.getSrcDirName() + "/jbake.properties"));
				task.getGeneratedJBakeConfiguration().set(getLayout().getBuildDirectory().file("tmp/" + task.getName() + "/jbake.properties"));
			});

			val stageTask = getTasks().register("stage", Sync.class, task -> {
				task.setDestinationDir(getLayout().getBuildDirectory().dir("stage").get().getAsFile());
				// Copy everything from the original location except for jbake.properties as it's processed separately
				task.from((Callable<String>) jbake::getSrcDirName, spec -> spec.exclude("jbake.properties"));

				// Copy content from dependencies
				task.from(processTask.flatMap(ProcessJBakeConfiguration::getGeneratedJBakeConfiguration));
				task.from(templates, spec -> spec.into("templates"));
				task.from(content, spec -> spec.into("content"));
				task.from(assets, spec -> spec.into("assets"));
			});
			bakeTask.configure(task -> {
				task.dependsOn(stageTask);
				task.setInput(stageTask.map(Sync::getDestinationDir).get());
			});

			// Outgoing
			val assetsElements = createOutgoingAssets(jbake);
			val templatesElements = createOutgoingTemplates(jbake);
			val contentElements = createOutgoingContent(jbake);
			val configurationElements = createOutgoingConfiguration(jbake);

			val jbakeComponent = getComponentFactory().adhoc("jbake");
			jbakeComponent.addVariantsFromConfiguration(assetsElements, this::configureVariant);
			jbakeComponent.addVariantsFromConfiguration(templatesElements, this::configureVariant);
			jbakeComponent.addVariantsFromConfiguration(contentElements, this::configureVariant);
			jbakeComponent.addVariantsFromConfiguration(configurationElements, this::configureVariant);
			getComponents().add(jbakeComponent);

			createOutgoingBaked(jbake, siteTask);
		}

		private Configuration createOutgoingAssets(JBakeExtension jbake) {
			val configuration = getConfigurations().create(ASSETS_ELEMENTS_CONFIGURATION_NAME);
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_ASSETS_USAGE_NAME));
			});

			val zipTask = getTasks().register(ZIP_ASSETS_TASK_NAME, Zip.class, task -> {
				task.from((Callable<String>) () -> jbake.getSrcDirName() + "/assets");
				task.getArchiveClassifier().set("assets");
			});
			configuration.getOutgoing().artifact(zipTask);

			return configuration;
		}

		private Configuration createOutgoingConfiguration(JBakeExtension jbake) {
			val configuration = getConfigurations().create(CONFIGURATION_ELEMENTS_CONFIGURATION_NAME);
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_CONFIGURATION_USAGE_NAME));
			});

			val jbakeProperties = getLayout().getProjectDirectory().file(jbake.getSrcDirName() + "/jbake.properties").getAsFile();
			if (jbakeProperties.exists()) {
				configuration.getOutgoing().artifact(jbakeProperties);
			}

			return configuration;
		}

		private Configuration createOutgoingContent(JBakeExtension jbake) {
			val configuration = getConfigurations().create(CONTENT_ELEMENTS_CONFIGURATION_NAME);
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_CONTENT_USAGE_NAME));
			});

			val zipTask = getTasks().register(ZIP_CONTENT_TASK_NAME, Zip.class, task -> {
				task.from((Callable<String>) () -> jbake.getSrcDirName() + "/content");
				task.getArchiveClassifier().set("content");
			});
			configuration.getOutgoing().artifact(zipTask);

			return configuration;
		}

		private Configuration createOutgoingTemplates(JBakeExtension jbake) {
			val configuration = getConfigurations().create(TEMPLATES_ELEMENTS_CONFIGURATION_NAME);
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_TEMPLATES_USAGE_NAME));
			});

			val zipTask = getTasks().register(ZIP_TEMPLATES_TASK_NAME, Zip.class, task -> {
				task.from((Callable<String>) () -> jbake.getSrcDirName() + "/templates");
				task.getArchiveClassifier().set("templates");
			});
			configuration.getOutgoing().artifact(zipTask);

			return configuration;
		}

		private void createOutgoingBaked(JBakeExtension jbake, TaskProvider<Sync> siteTask) {
			val configuration = getConfigurations().create(BAKED_ELEMENTS_CONFIGURATION_NAME);
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, JBAKE_BAKED_USAGE_NAME));
			});

			val zipTask = getTasks().register(ZIP_BAKED_TASK_NAME, Zip.class, task -> {
				task.from(siteTask);
				task.getArchiveClassifier().set("baked");
			});
			configuration.getOutgoing().artifact(zipTask);

			configuration.getOutgoing().getVariants().create("directory", variant -> {
				variant.artifact(siteTask.map(Sync::getDestinationDir), it -> {
					it.setType(DIRECTORY_TYPE);
					it.builtBy(siteTask);
				});
			});

			val component = getComponentFactory().adhoc("baked");
			component.addVariantsFromConfiguration(configuration, this::configureVariant);
			getComponents().add(component);
		}

		private void configureVariant(ConfigurationVariantDetails variantDetails) {
			if (hasUnpublishableArtifactType(variantDetails.getConfigurationVariant())) {
				variantDetails.skip();
			}
		}

		public boolean hasUnpublishableArtifactType(ConfigurationVariant element) {
			for (PublishArtifact artifact : element.getArtifacts()) {
				if (ArtifactTypeDefinition.DIRECTORY_TYPE.equals(artifact.getType())) {
					return true;
				}
			}
			return false;
		}
	}
}
