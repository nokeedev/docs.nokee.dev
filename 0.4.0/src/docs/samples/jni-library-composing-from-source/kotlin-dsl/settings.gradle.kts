pluginManagement {
	repositories {
		gradlePluginPortal()
		maven { url = uri("https://repo.nokee.dev/release") }
		maven { url = uri("https://repo.nokee.dev/snapshot") }
	}
	val nokeeVersion = "0.4.0"
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id.startsWith("dev.nokee.")) {
				useModule("${requested.id.id}:${requested.id.id}.gradle.plugin:${nokeeVersion}")
			}
		}
	}
}

rootProject.name = "jni-library-composing-from-source"

include("java-jni-greeter")
include("cpp-jni-greeter")
include("java-loader")
include("cpp-greeter")
