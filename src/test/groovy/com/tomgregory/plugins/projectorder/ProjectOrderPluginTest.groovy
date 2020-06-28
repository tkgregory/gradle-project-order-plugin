package com.tomgregory.plugins.projectorder

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ProjectOrderPluginTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File settingsFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.project-order'
            }
            
            projectOrder {
                taskName = 'sayHi'
            }
        """
    }

    def 'controls execution order between tasks in multiple projects using suffix'() {
        given:
        createProjects('project1', 'project2', 'project3')
        when:
        BuildResult result = runTask('sayHi')
        then:
        taskPaths(result) == [':project1:sayHi', ':project2:sayHi', ':project3:sayHi']
    }

    def 'controls execution order between tasks in multiple projects when projects declared out of order using suffix'() {
        given:
        createProjects('project2', 'project3', 'project1')
        when:
        BuildResult result = runTask('sayHi')
        then:
        taskPaths(result) == [':project1:sayHi', ':project2:sayHi', ':project3:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using numeric prefix'() {
        given:
        createProjects('bproject', 'aproject', 'cproject')
        when:
        BuildResult result = runTask('sayHi')
        then:
        taskPaths(result) == [':aproject:sayHi', ':bproject:sayHi', ':cproject:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using prefix and suffix'() {
        given:
        createProjects('project3', 'bproject', 'project1', 'aproject', 'project2', 'cproject')
        when:
        BuildResult result = runTask('sayHi')
        then:
        taskPaths(result) == [':aproject:sayHi', ':bproject:sayHi', ':cproject:sayHi', ':project1:sayHi', ':project2:sayHi', ':project3:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using double-digit numeric prefix'() {
        given:
        createProjects('0-project', '1-project', '2-project', '10-project', '11-project', '12-project')
        when:
        BuildResult result = runTask('sayHi')
        then:
        taskPaths(result) == [':0-project:sayHi', ':1-project:sayHi', ':2-project:sayHi', ':10-project:sayHi', ':11-project:sayHi', ':12-project:sayHi']
    }

    private void createProjects(String... projectNames) {
        projectNames.each { projectName ->
            createProject(projectName)
        }
    }

    private void createProject(String projectName) {
        File projectFolder = testProjectDir.newFolder(projectName)
        File projectBuildFile = new File(projectFolder, 'build.gradle')
        projectBuildFile << """
            task sayHi {
                doLast {
                    println "My name is \${project.name}"
                }
            }
        """

        settingsFile << """
            include '$projectName'
        """
    }

    private BuildResult runTask(String taskName) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(taskName, '--stacktrace')
                .withPluginClasspath()
                .withDebug(true)
                .forwardOutput()
                .build()
    }

    private List<String> taskPaths(BuildResult buildResult) {
        return buildResult.tasks.collect { element -> element.path }
    }
}
