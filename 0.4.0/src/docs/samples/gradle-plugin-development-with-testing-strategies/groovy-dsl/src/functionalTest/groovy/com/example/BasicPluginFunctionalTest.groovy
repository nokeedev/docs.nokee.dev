package com.example
import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class BasicPluginFunctionalTest extends AbstractGradleSpecification {
    def "can do basic test"() {
        given:
        buildFile << '''
            plugins {
                id('com.example.hello')
            }
        '''

        when:
        succeeds('help')

        then:
        result.output.contains('Hello')
    }
}
