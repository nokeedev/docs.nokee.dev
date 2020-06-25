package dev.nokee.docs.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement

class JavadocGreeter extends JavaSourceFileElement {
	@Override
	SourceFileElement getSource() {
		return SourceFileElement.ofFile(sourceFile('java/com/example', 'Greeter.java', '''
package com.example;

/**
 * Greeting message.
 * @since 1.0
 */
public class Greeter {
	/**
	 * Return {@code Bonjour, $name!}.
	 * @param name the person to say hello to.
	 * @return a greeting message, never null.
	 */
	public String sayHello(String name) {
		return "Bonjour, " + name + "!";
	}
}
		'''))
	}
}
