package dev.nokee.docs.tasks;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import dev.nokee.docs.JavadocDependencyLock;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public abstract class GenerateJavadocClasspathLock extends DefaultTask {
	@Nested
	public abstract SetProperty<JavadocDependencyLock.Dependency> getDependencies();

	@OutputFile
	public abstract RegularFileProperty getGeneratedJavadocClasspathLock();

	@TaskAction
	private void doGenerate() throws IOException {
		val xmlMapper = new XmlMapper();
		xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
		xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
		xmlMapper.writeValue(getGeneratedJavadocClasspathLock().get().getAsFile(), new JavadocDependencyLock(getDependencies().get()));
	}
}
