package dev.nokee.docs;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@JacksonXmlRootElement(localName = "dependencies")
@AllArgsConstructor
public class JavadocDependencyLock {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "dependency")
	private Collection<Dependency> dependencies;

	public JavadocDependencyLock() {
		this(Collections.emptyList());
	}

	@Data
	@AllArgsConstructor
	public static class Dependency {
		@Input
		private String groupId;

		@Input
		private String artifactId;

		@Input
		private String version;

		public Dependency() {
			this(null, null, null);
		}
	}
}
