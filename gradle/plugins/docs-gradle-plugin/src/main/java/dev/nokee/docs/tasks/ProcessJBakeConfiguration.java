package dev.nokee.docs.tasks;

import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Buildable;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.*;
import java.util.*;

public abstract class ProcessJBakeConfiguration extends DefaultTask {
	private final List<Buildable> buildableElements = new ArrayList<>();

	@Input
	public abstract MapProperty<String, Object> getConfiguration();

	@OutputFile
	public abstract RegularFileProperty getGeneratedJBakeConfiguration();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	public ProcessJBakeConfiguration() {
		dependsOn(buildableElements);
	}

	@TaskAction
	private void doProcess() throws IOException {
		val properties = new Properties();
		properties.putAll(getConfiguration().get());

		try (PrintWriter out = new PrintWriter(getGeneratedJBakeConfiguration().get().getAsFile())) {
			properties.store(out, "");
		}
	}

	public void from(Object obj) {
		if (obj instanceof FileCollection) {
			fromFileCollection((FileCollection) obj);
		} else if (obj instanceof RegularFile) {
			fromFile(((RegularFile) obj).getAsFile());
		}
	}

	private void fromFileCollection(FileCollection files) {
		buildableElements.add(files);
		getConfiguration().putAll(files.getElements().map(this::load));
	}

	private Map<String, Object> load(Set<? extends FileSystemLocation> files) {
		val result = new HashMap<String, Object>();
		for (FileSystemLocation fileLocation : files) {
			loadTo(fileLocation.getAsFile(), result);
		}
		return result;
	}

	@SneakyThrows
	private void loadTo(File file, Map<String, Object> result) {
		val properties = new Properties();
		try (InputStream inStream = new FileInputStream(file)) {
			properties.load(inStream);
			properties.forEach((v, k) -> {
				result.put(v.toString(), k);
			});
		}
	}

	private void fromFile(File file) {
		if (file.exists()) {
			getConfiguration().putAll(getProviders().provider(() -> {
				val result = new HashMap<String, Object>();
				loadTo(file, result);
				return result;
			}));
		}
	}
}
