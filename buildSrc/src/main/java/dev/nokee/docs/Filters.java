package dev.nokee.docs;

import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileSystemLocation;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Filters {
	@SneakyThrows
	public static Iterable<String> findAllHtmlFiles(Iterable<? extends FileSystemLocation> directories) {
		val result = new ArrayList<String>();
		for (FileSystemLocation directory : directories) {
			val baseDirectory = directory.getAsFile().toPath();
			Files.walkFileTree(baseDirectory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.getFileName().toString().endsWith(".html")) {
						result.add(baseDirectory.relativize(file).toString());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		return result;
	}

	private static final Set<String> REDIRECTION_PAGES = new HashSet<>(Arrays.asList("docs/0.1.0/index.html", "docs/0.1.0/manual/index.html", "docs/0.2.0/index.html", "docs/0.2.0/manual/index.html", "docs/0.3.0/index.html", "docs/0.3.0/manual/index.html"));
	public static Iterable<String> withoutRedirectionPages(Iterable<? extends String> entries) {
		return StreamSupport.stream(entries.spliterator(), false).filter(it -> !REDIRECTION_PAGES.contains(it)).collect(Collectors.toList());
	}

	public static Iterable<String> asCanonicalPaths(Iterable<? extends String> entries) {
		val result = new ArrayList<String>();
		// Make sure we use the canonical path
		for (String entry : entries) {
			if (entry.endsWith("/index.html")) {
				result.add(entry.substring(0, entry.lastIndexOf('/') + 1));
			} else if (entry.equals("index.html")) {
				result.add("");
			} else {
				result.add(entry);
			}
		}
		return result;
	}

	public static Transformer<Iterable<URL>, Iterable<? extends String>> withHost(String host) {
		return new Transformer<Iterable<URL>, Iterable<? extends String>>() {
			@SneakyThrows
			@Override
			public Iterable<URL> transform(Iterable<? extends String> entries) {
				val result = new ArrayList<URL>();
				for (String entry : entries) {
					result.add(new URL("https://" + host + "/" + entry));
				}
				return result;
			}
		};
	}

	@SneakyThrows
	public static Iterable<Sitemap.Url> toSitemapUrl(Iterable<? extends URL> entries) {
		val result = new ArrayList<Sitemap.Url>();
		for (URL entry : entries) {
			result.add(new Sitemap.Url(entry, LocalDate.now()));
		}
		return result;
	}
}
