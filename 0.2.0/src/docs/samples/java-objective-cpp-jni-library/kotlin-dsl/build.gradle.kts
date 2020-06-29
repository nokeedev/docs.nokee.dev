plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.objective-cpp-language")
}

// Internal details, will be fixed in future commit
tasks.withType<LinkSharedLibrary> {
	linkerArgs.add("-lobjc")
}