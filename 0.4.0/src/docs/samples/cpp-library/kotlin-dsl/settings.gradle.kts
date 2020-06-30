pluginManagement {
	repositories {
		gradlePluginPortal()
		maven { url = uri("https://dl.bintray.com/nokeedev/distributions") }
		maven { url = uri("https://dl.bintray.com/nokeedev/distributions-snapshots") }
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

rootProject.name = "cpp-library"
