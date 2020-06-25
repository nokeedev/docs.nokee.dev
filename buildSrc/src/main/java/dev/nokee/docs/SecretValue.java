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

		return getParameters().getDefaultValue().getOrNull();
	}

	public interface OrDefaultConfiguration {
		void orDefault(String defaultValue);
	}

	public interface PropertyOrDefaultConfiguration extends OrDefaultConfiguration {
		OrDefaultConfiguration or(String propertyName);
	}

	public interface PropertySource extends ValueSourceParameters, PropertyOrDefaultConfiguration {
		Property<String> getPropertyName();
		Property<String> getEnvironmentVariableName();
		Property<String> getDefaultValue();

		default OrDefaultConfiguration or(String propertyName) {
			getPropertyName().set(propertyName);
			return this;
		}

		default PropertyOrDefaultConfiguration from(String environmentVariableName) {
			getEnvironmentVariableName().set(environmentVariableName);
			return this;
		}

		default void orDefault(String defaultValue) {
			getDefaultValue().set(defaultValue);
		}
	}
}
