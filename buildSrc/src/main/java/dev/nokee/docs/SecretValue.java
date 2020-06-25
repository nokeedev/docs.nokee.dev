package dev.nokee.docs;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;

public abstract class SecretValue implements ValueSource<String, SecretValue.PropertySource> {
	@Inject
	protected abstract ProviderFactory getProviders();

	@Nullable
	@Override
	public String obtain() {
		Object propValue = System.getenv().get(getParameters().getEnvironmentVariableName().get());

		if (propValue != null) {
			return propValue.toString();
		}

		propValue = getProviders().gradleProperty(getParameters().getPropertyName()).getOrNull();
		if (propValue != null) {
			return propValue.toString();
		}

		propValue = getProviders().systemProperty(getParameters().getPropertyName()).getOrNull();
		if (propValue != null) {
			return propValue.toString();
		}

		return null;
	}

	public interface PropertySource extends ValueSourceParameters {
		Property<String> getPropertyName();
		Property<String> getEnvironmentVariableName();

		default PropertySource or(String propertyName) {
			getPropertyName().set(propertyName);
			return this;
		}

		default PropertySource from(String environmentVariableName) {
			getEnvironmentVariableName().set(environmentVariableName);
			return this;
		}
	}
}
