package dev.nokee.docs.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement

class JavadocGreeterWithCommonLang extends JavaSourceFileElement {
	@Override
	SourceFileElement getSource() {
		return SourceFileElement.ofFile(sourceFile('java/com/example', 'Greeter.java', '''
package com.example;

import org.apache.commons.lang3.StringUtils;

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
		return "Bonjour, " + StringUtils.capitalize(name) + "!";
	}
}
		'''))
	}
}
