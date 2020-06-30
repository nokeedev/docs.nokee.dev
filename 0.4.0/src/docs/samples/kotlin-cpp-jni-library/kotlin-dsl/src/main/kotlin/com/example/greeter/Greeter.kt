
package com.example.greeter

class Greeter {
	companion object {
		init {
			NativeLoader.loadLibrary(Greeter::class.java.classLoader, "kotlin-cpp-jni-library")
		}
	}

	external fun sayHello(name: String?): String?
}
