package dev.nokee.docs.tasks;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class GenerateVersionIndex extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@Input
	public abstract SetProperty<String> getVersions();

	@TaskAction
	private void doGenerate() throws IOException {
		FileUtils.deleteDirectory(getDestinationDirectory().get().getAsFile());
		getDestinationDirectory().get().getAsFile().mkdirs();

		val versions = new ArrayList<>(getVersions().get());
		versions.sort(Comparator.reverseOrder());

		val it = versions.iterator();
		val latest = it.next();

		try (PrintWriter out = new PrintWriter(getDestinationDirectory().file("latest.adoc").get().getAsFile())) {
			out.println("[%header, cols=\"2,5a\"]");
			out.println("|===");
			out.println("| Version | Format");
			out.println("");
			out.println("| v" + latest);
			out.println("|");
			out.println("- link:" + latest + "/manual/user-manual.html[User manual]");
			out.println("- link:" + latest + "/samples[Samples]");
			out.println("- link:" + latest + "/manual/plugin-references.html[Plugin references]");
			out.println("|===");
		}

		try (PrintWriter out = new PrintWriter(getDestinationDirectory().file("archived.adoc").get().getAsFile())) {
			out.println("[%header, cols=\"2,5a\"]");
			out.println("|===");
			out.println("| Version | Format");
			it.forEachRemaining(version -> {
				out.println("");
				out.println("| v" + version);
				out.println("|");
				out.println("- link:" + version + "/manual/user-manual.html[User manual]");
				out.println("- link:" + version + "/samples[Samples]");
				out.println("- link:" + version + "/manual/plugin-references.html[Plugin references]");
			});
			out.println("|===");
		}

		// Ignore these files during baking
		getDestinationDirectory().get().file(".jbakeignore").getAsFile().createNewFile();
	}
}
