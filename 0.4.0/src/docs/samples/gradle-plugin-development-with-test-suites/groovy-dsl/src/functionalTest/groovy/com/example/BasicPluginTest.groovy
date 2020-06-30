package com.example

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BasicPluginFunctionalTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File settingsFile
    File buildFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "can do basic test"() {
        given:
        settingsFile << "rootProject.name = 'hello-world'"
        buildFile << '''
            plugins {
                id('com.example.hello')
            }
        '''

        when:
        def result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('help')
            .build()

        then:
        result.output.contains('Hello')
    }
}
