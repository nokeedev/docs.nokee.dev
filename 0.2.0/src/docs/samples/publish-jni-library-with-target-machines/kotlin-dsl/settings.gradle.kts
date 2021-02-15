pluginManagement {
	repositories {
		gradlePluginPortal()
		maven { url = uri("https://repo.nokeedev.net/release") }
		maven { url = uri("https://repo.nokeedev.net/snapshot") }
	}
	val nokeeVersion = "0.2.0"
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id.startsWith("dev.nokee.")) {
				useModule("${requested.id.id}:${requested.id.id}.gradle.plugin:${nokeeVersion}")
			}
		}
	}
}

rootProject.name = "publish-jni-library-with-target-machines"

includeBuild("gradle/plugins/jni-library-publish")
