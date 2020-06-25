package dev.nokee.docs;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.gradle.api.tasks.Input;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;

@Value
@JacksonXmlRootElement(localName = "urlset")
public class Sitemap {
	@JacksonXmlProperty(isAttribute = true)
	@Getter(AccessLevel.PRIVATE) String xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9";

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "url")
	Collection<Url> urls;

	@Value
	public static class Url {
		@Input
		@JacksonXmlProperty(localName = "loc")
		URL location;

		@Input
		@JacksonXmlProperty(localName = "lastmod")
		LocalDate lastModified;
	}
}
