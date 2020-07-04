package com.tomgregory.plugins.projectorder

import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.tomgregory.plugins.projectorder.TestUtil.executedTaskPaths
import static com.tomgregory.plugins.projectorder.TestUtil.runTasks

class StandaloneExampleTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    def setup() {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.project-order'
            }
            
            projectOrder {
                taskNames = ['deploy']
            }
            
            subprojects {
                task deploy {
                    doLast {
                        println "Running deploy task in \${project.name}"
                    }
                }
            }
        """

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include '1-networking-resources',
                    '2-security-resources',
                    '3-computer-resources'
        """
    }

    def 'runs tasks in order'() {
        when:
        BuildResult result = runTasks(testProjectDir.root, 'deploy')
        then:
        executedTaskPaths(result) == [':1-networking-resources:deploy', ':2-security-resources:deploy', ':3-computer-resources:deploy']
    }
}
