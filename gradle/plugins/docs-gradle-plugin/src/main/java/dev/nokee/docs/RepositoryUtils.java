package dev.nokee.docs;

import org.gradle.api.Action;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.AwsCredentials;

import static dev.nokee.docs.SecretValue.obtainSecret;

public class RepositoryUtils {
	public static Action<? super MavenArtifactRepository> nokeeDocumentation() {
		return repository -> {
			repository.setName("Nokee Documentation on S3");
			repository.credentials(AwsCredentials.class, credentials -> {
				credentials.setAccessKey(obtainSecret("AWS_ACCESS_KEY", "dev.nokee.aws.user"));
				credentials.setSecretKey(obtainSecret("AWS_SECRET_KEY", "dev.nokee.aws.key"));
			});
			repository.setUrl("s3://docs.nokee.dev/");
		};
	}
}
