
package com.example.greeter

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class GreeterTest {
	@Test
	fun testGreeter() {
		val greeter = Greeter()
		val greeting = greeter.sayHello("World")
		assertThat(greeting, equalTo("Bonjour, World!"))
	}

	@Test
	fun testNullGreeter() {
		val greeter = Greeter()
		val greeting = greeter.sayHello(null)
		assertThat(greeting, equalTo("name cannot be null"))
	}
}
