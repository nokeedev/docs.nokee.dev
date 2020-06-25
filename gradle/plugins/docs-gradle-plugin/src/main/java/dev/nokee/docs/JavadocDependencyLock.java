package dev.nokee.docs;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gradle.api.tasks.Input;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

@Data
@JacksonXmlRootElement(localName = "javadoc-locking")
@AllArgsConstructor
public class JavadocDependencyLock {
	@JacksonXmlElementWrapper(localName = "dependencies")
	@JacksonXmlProperty(localName = "dependency")
	private Collection<Dependency> dependencies;

	@JacksonXmlElementWrapper(localName = "repositories")
	@JacksonXmlProperty(localName = "repository")
	private Collection<Repository> repositories;

	public JavadocDependencyLock() {
		this(Collections.emptyList(), Collections.emptyList());
	}

	@Data
	@AllArgsConstructor
	public static class Repository {
		@Input
		private URI url;

		public Repository() {
			this(null);
		}
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
