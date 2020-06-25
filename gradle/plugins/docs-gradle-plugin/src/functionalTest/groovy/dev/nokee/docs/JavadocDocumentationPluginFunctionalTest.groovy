package dev.nokee.docs

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.docs.fixtures.JavadocGreeter
import dev.nokee.docs.fixtures.JavadocGreeterWithCommonLang

class JavadocDocumentationPluginFunctionalTest extends AbstractGradleSpecification {
	// Applying to java-base project will configure outgoing artifact for source (javadocSourceElements) as zip and directory (this should be source jar)
	// Applying to java-base project will configure outgoing artifact for dependencies (kind of lock file) (javadocClasspathElements) as json file
	// Configures outgoing artifact for baked javadoc (javadocBakedElements) as zip and directory
	// Creates component javadoc
	// Creates javadoc configuration as a bucket and javadocClasspath + javadocSource as resolvable

	/*
	In site generator:
	plugins {
		id 'dev.nokee.documentation.javadoc'
	}

	dependencies {
		javadoc project(':blah')
		javadoc project(':foo')
	}

	tasks.named('javadoc') {
		more configuration
	}
	 */

	def "skips javadoc when no source"() {
		given:
		buildFile << """
			plugins {
				id 'dev.nokee.documentation.javadoc'
			}
		"""

		expect:
		succeeds('javadoc')
		result.assertTaskSkipped(':javadoc')
	}

	def "can generate javadoc when source comes from source set"() {
		given:
		buildFile << """
			plugins {
				id 'dev.nokee.documentation.javadoc'
				id 'java-library'
			}
		"""

		and:
		new JavadocGreeter().writeToProject(testDirectory)

		and:
		file('build/docs/javadoc/com/example/Greeter.html').assertDoesNotExist()

		when:
		succeeds('javadoc')

		then:
		result.assertTaskNotSkipped(':javadoc')
		file('build/docs/javadoc/com/example/Greeter.html').assertExists()
	}

	def "can generate javadoc when source comes from dependencies"() {
		given:
		buildFile << """
			plugins {
				id 'dev.nokee.documentation.javadoc'
			}

			dependencies {
				javadocSource(fileTree('src/main/java') { include '**/*.java' })
			}
		"""

		and:
		new JavadocGreeter().writeToProject(testDirectory)

		and:
		file('build/docs/javadoc/com/example/Greeter.html').assertDoesNotExist()

		when:
		succeeds('javadoc')

		then:
		result.assertTasksExecutedAndNotSkipped(':javadoc')
		file('build/docs/javadoc/com/example/Greeter.html').assertExists()
	}

	def "can generate javadoc with additional classpath"() {
		given:
		buildFile << """
			plugins {
				id 'dev.nokee.documentation.javadoc'
			}

			repositories {
				mavenCentral()
			}

			dependencies {
				javadocSource(fileTree('src/main/java') { include '**/*.java' })
				javadocClasspath 'org.apache.commons:commons-lang3:3.9'
			}
		"""

		and:
		new JavadocGreeterWithCommonLang().writeToProject(testDirectory)

		and:
		file('build/docs/javadoc/com/example/Greeter.html').assertDoesNotExist()

		when:
		succeeds('javadoc')

		then:
		result.assertTasksExecutedAndNotSkipped(':javadoc')
		file('build/docs/javadoc/com/example/Greeter.html').assertExists()
	}

	def "can generate javadoc with dependency lock"() {
		given:
		buildFile << """
			plugins {
				id 'dev.nokee.documentation.javadoc'
			}

			repositories {
				mavenCentral()
			}

			dependencies {
				javadocSource(fileTree('src/main/java') { include '**/*.java' })
				javadocClasspathLock files('javadoc-lock.xml')
			}
		"""

		and:
		file('javadoc-lock.xml') << '''<?xml version='1.0' encoding='UTF-8'?>
			|<dependencies>
			|    <dependency>
			|        <groupId>org.apache.commons</groupId>
			|        <artifactId>commons-lang3</artifactId>
			|        <version>3.9</version>
			|	</dependency>
			|</dependencies>
			|'''.stripMargin()

		and:
		new JavadocGreeterWithCommonLang().writeToProject(testDirectory)

		and:
		file('build/docs/javadoc/com/example/Greeter.html').assertDoesNotExist()

		when:
		succeeds('javadoc')

		then:
		result.assertTasksExecutedAndNotSkipped(':javadoc')
		file('build/docs/javadoc/com/example/Greeter.html').assertExists()
	}

	def "can generate javadoc classpath lock"() {
		given:
		buildFile << """
			plugins {
				id 'java-library'
				id 'dev.nokee.documentation.javadoc'
			}

			dependencies {
				implementation 'org.apache.commons:commons-lang3:3.9'
			}
		"""

		and:
		new JavadocGreeterWithCommonLang().writeToProject(testDirectory)

		when:
		succeeds('generateJavadocClasspathLock')

		then:
		result.assertTasksExecutedAndNotSkipped(':generateJavadocClasspathLock')
		file('build/tmp/generateJavadocClasspathLock/javadoc-classpath-lock.xml').text == '''<?xml version='1.0' encoding='UTF-8'?>
			|<dependencies>
			|  <dependency>
			|    <groupId>org.apache.commons</groupId>
			|    <artifactId>commons-lang3</artifactId>
			|    <version>3.9</version>
			|  </dependency>
			|</dependencies>
			|'''.stripMargin()
	}
}
