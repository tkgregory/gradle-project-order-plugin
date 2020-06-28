package com.tomgregory.plugins.projectorder

import org.gradle.api.Project
import spock.lang.Specification

class ProjectComparatorTest extends Specification {

    ProjectComparator projectComparator = new ProjectComparator()

    def 'sorts by alphanumeric value'() {
        given:
        List<Project> projects = createProjects('project2', 'project1', 'project3')
        when:
        List<Project> sorted = projects.toSorted(projectComparator);
        then:
        sorted.collect { it.name } == ['project1', 'project2', 'project3']
    }

    def 'sorts by numeric prefix'() {
        given:
        List<Project> projects = createProjects('2-project', '1-project', '3-project')
        when:
        List<Project> sorted = projects.toSorted(projectComparator);
        then:
        sorted.collect { it.name } == ['1-project', '2-project', '3-project']
    }

    def 'sorts by double digit numeric prefix'() {
        given:
        List<Project> projects = createProjects('25-project', '10-project', '22-project')
        when:
        List<Project> sorted = projects.toSorted(projectComparator);
        then:
        sorted.collect { it.name } == ['10-project', '22-project', '25-project']
    }

    def 'sorts by triple digit numeric prefix'() {
        given:
        List<Project> projects = createProjects('999-project', '57-project', '156-project')
        when:
        List<Project> sorted = projects.toSorted(projectComparator);
        then:
        sorted.collect { it.name } == ['57-project', '156-project', '999-project']
    }

    def 'sorts by numeric prefix and alphanumeric suffix'() {
        given:
        List<Project> projects = createProjects('12-project', '11-b-project', '11-a-project', '10-project')
        when:
        List<Project> sorted = projects.toSorted(projectComparator);
        then:
        sorted.collect { it.name } == ['10-project', '11-a-project', '11-b-project', '12-project']
    }

    def 'sorts by numeric then alphanumeric'() {
        given:
        List<Project> projects = createProjects('a-project', '1-project', 'b-project', '2-project')
        when:
        List<Project> sorted = projects.toSorted(projectComparator);
        then:
        sorted.collect { it.name } == ['1-project', '2-project', 'a-project', 'b-project']
    }

    private List<Project> createProjects(String... projectNames) {
        return projectNames.collect { projectName ->
            createProject(projectName)
        }
    }

    private Project createProject(String projectName) {
        Project project = Mock(Project.class)
        project.getName() >> projectName
        return project
    }
}
