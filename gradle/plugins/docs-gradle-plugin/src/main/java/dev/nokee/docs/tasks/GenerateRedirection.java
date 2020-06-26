package dev.nokee.docs.tasks;

import lombok.Value;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class GenerateRedirection extends DefaultTask {
	@Nested
	public abstract SetProperty<Redirection> getRedirections();

	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@TaskAction
	private void doGenerate() throws IOException {
		FileUtils.deleteDirectory(getDestinationDirectory().get().getAsFile());

		val dir = getDestinationDirectory().get();
		for (Redirection redirection : getRedirections().get()) {
			val file = dir.file("content/" + redirection.getFrom() + "/index.adoc").getAsFile();
			file.getParentFile().mkdirs();
			try (PrintWriter out = new PrintWriter(file)) {
				val from = dir.file(redirection.getFrom()).getAsFile().toPath();
				val to = dir.file(redirection.getTo()).getAsFile().toPath();
				out.println(":jbake-type: redirection");
				out.println(":jbake-redirecturl: " + from.relativize(to).toString());
				out.println(":jbake-status: published");
			}
		}

		// TODO: Write redirection template
		// TODO: Write jbake.properties with configuration for template (it needs to be merged with)
	}

	public GenerateRedirection redirect(String from, String to) {
		getRedirections().add(new Redirection(from, to));
		return this;
	}

	@Value
	public static class Redirection {
		@Input
		String from;

		@Input
		String to;
	}
}
