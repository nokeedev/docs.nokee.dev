package dev.nokee.docs;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;

public abstract class SecretValue implements ValueSource<String, SecretValue.PropertySource> {
	@Nullable
	@Override
	public String obtain() {
		Object propValue = System.getenv().get(getParameters().getEnvironmentVariableName().get());

		if (propValue != null) {
			return propValue.toString();
		}

		propValue = System.getProperty(getParameters().getPropertyName().get());
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
