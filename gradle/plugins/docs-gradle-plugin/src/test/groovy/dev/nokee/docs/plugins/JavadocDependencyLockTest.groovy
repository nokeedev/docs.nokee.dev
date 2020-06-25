package dev.nokee.docs.plugins

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import dev.nokee.docs.JavadocDependencyLock
import spock.lang.Specification
import spock.lang.Subject

@Subject(JavadocDependencyLock)
class JavadocDependencyLockTest extends Specification {
	def "can serialize javadoc dependency lock"() {
		given:
		def dependency = new JavadocDependencyLock.Dependency('com.example', 'artifact', '1.0')
		def dependencies = new JavadocDependencyLock([dependency], [])
		def xmlMapper = new XmlMapper()

		when:
		def result = xmlMapper.writeValueAsString(dependencies)

		then:
		result == '<javadoc-locking><dependencies><dependency><groupId>com.example</groupId><artifactId>artifact</artifactId><version>1.0</version></dependency></dependencies><repositories/></javadoc-locking>'
	}
}
