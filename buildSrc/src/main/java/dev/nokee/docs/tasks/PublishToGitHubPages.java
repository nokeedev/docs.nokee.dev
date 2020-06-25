package dev.nokee.docs.tasks;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public abstract class PublishToGitHubPages extends DefaultTask {
	@InputDirectory
	public abstract DirectoryProperty getPublishDirectory();

	@Input
	public abstract Property<String> getGitHubKey();

	@Input
	public abstract Property<String> getGitHubSecret();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void doPublish() throws GitAPIException, IOException {
		if (isGitRepository(getTemporaryDir())) {
			try (Git git = Git.open(getTemporaryDir())) {
				git.fetch().setTagOpt(TagOpt.NO_TAGS).call();
				git.reset().setMode(ResetCommand.ResetType.HARD).setRef("refs/heads/gh-pages").call();
				git.checkout().setName("gh-pages").call();
			}
		} else {
			Git.cloneRepository().setDirectory(getTemporaryDir()).setURI("https://github.com/nokeedev/docs.nokee.dev.git").setBranch("gh-pages").setNoTags().call();
		}

		getFileOperations().sync(spec -> {
			spec.into(getTemporaryDir());
			spec.from(getPublishDirectory());
		});

		try (Git git = Git.open(getTemporaryDir())) {
			git.add().addFilepattern(".").call();
			git.commit().setAuthor("nokeedevbot", "bot@nokee.dev").setMessage("Publish by nokeedevbot").call();
			git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(getGitHubKey().get(), getGitHubSecret().get())).call();
		}
	}

	private static boolean isGitRepository(File repositoryDirectory) {
		try {
			return new FileRepository(new File(repositoryDirectory, ".git")).getObjectDatabase().exists();
		} catch (IOException e) {
			return false;
		}
	}
}
