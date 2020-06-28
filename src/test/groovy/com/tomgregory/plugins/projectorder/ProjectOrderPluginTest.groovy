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
    }

    def 'controls execution order between tasks in multiple projects using suffix'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('project1', 'project2', 'project3')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':project1:sayHi', ':project2:sayHi', ':project3:sayHi']
    }

    def 'controls execution order between tasks in multiple projects when projects declared out of order using suffix'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('project2', 'project3', 'project1')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':project1:sayHi', ':project2:sayHi', ':project3:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using numeric prefix'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('bproject', 'aproject', 'cproject')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':aproject:sayHi', ':bproject:sayHi', ':cproject:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using prefix and suffix'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('project3', 'bproject', 'project1', 'aproject', 'project2', 'cproject')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':aproject:sayHi', ':bproject:sayHi', ':cproject:sayHi', ':project1:sayHi', ':project2:sayHi', ':project3:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using double-digit numeric prefix'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('0-project', '1-project', '2-project', '10-project', '11-project', '12-project')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':0-project:sayHi', ':1-project:sayHi', ':2-project:sayHi', ':10-project:sayHi', ':11-project:sayHi', ':12-project:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using triple-digit numeric prefix'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('57-project', '156-project', '999-project')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':57-project:sayHi', ':156-project:sayHi', ':999-project:sayHi']
    }

    def 'controls execution order between tasks in multiple projects using numeric prefix and alphanumeric suffix'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('10-project', '11-b-project', '11-a-project', '12-project')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':10-project:sayHi', ':11-a-project:sayHi', ':11-b-project:sayHi', ':12-project:sayHi']
    }

    def 'controls execution order between tasks in multiple projects by numeric then alphanumeric'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('a-project', '1-project', 'b-project', '2-project')
        when:
        BuildResult result = runTasks('sayHi')
        then:
        executedTaskPaths(result) == [':1-project:sayHi', ':2-project:sayHi', ':a-project:sayHi', ':b-project:sayHi']
    }

    def 'controls execution order between multiple tasks in multiple projects'() {
        given:
        buildFileForTaskNames('sayHi', 'sayBye')
        createProjects('project2', 'project3', 'project1')
        when:
        BuildResult result = runTasks('sayHi', 'sayBye')
        then:
        executedTaskPaths(result) == [':project1:sayHi', ':project2:sayHi', ':project3:sayHi', ':project1:sayBye', ':project2:sayBye', ':project3:sayBye']
    }

    def 'controls execution order but does not force dependsOn relationship between tasks'() {
        given:
        buildFileForTaskNames('sayHi')
        createProjects('project2', 'project3', 'project1')
        when:
        BuildResult result = runTasks('project2:sayHi')
        then:
        executedTaskPaths(result) == [':project2:sayHi']
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
                    println "Hi from \${project.name}"
                }
            }
            
            task sayBye {
                doLast {
                    println "Bye from \${project.name}"
                }
                mustRunAfter sayHi
            }
        """

        settingsFile << """
            include '$projectName'
        """
    }

    private BuildResult runTasks(String... taskNames) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(taskNames)
                .withPluginClasspath()
                .withDebug(true)
                .build()
    }

    private List<String> executedTaskPaths(BuildResult buildResult) {
        return buildResult.tasks.collect { element -> element.path }
    }

    private void buildFileForTaskNames(String... taskNames) {
        String taskNameConfig = taskNames
                .collect { taskName ->
                    return "'$taskName'"
                }
                .join(',')

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.project-order'
            }
            
            projectOrder {
                taskNames = [$taskNameConfig]
            }
        """
    }
}
