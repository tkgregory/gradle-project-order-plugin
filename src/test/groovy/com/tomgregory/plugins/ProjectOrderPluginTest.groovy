package com.plugins

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
        buildFile = testProjectDir.newFile('build.gradle')
        settingsFile = testProjectDir.newFile('settings.gradle')
    }

    def "can guarantee execution order between tasks in multiple projects"() {
        given:
        createProjects('project1', 'project2', 'project3')

        when:
        BuildResult result = runTask('sayMyName')

        then:
        result.tasks.size() == 3
        result.tasks[0].path == ":project1:sayMyName"
        result.tasks[1].path == ":project2:sayMyName"
        result.tasks[2].path == ":project3:sayMyName"
    }

    def "can guarantee execution order between tasks in multiple projects when projects declared out of order"() {
        given:
        createProjects('project2', 'project3', 'project1')

        when:
        BuildResult result = runTask('sayMyName')

        then:
        result.tasks.size() == 3
        result.tasks[0].path == ":project1:sayMyName"
        result.tasks[1].path == ":project2:sayMyName"
        result.tasks[2].path == ":project3:sayMyName"
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
            task sayMyName {
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
                .withArguments(taskName)
                .withPluginClasspath()
                .build()
    }
}
