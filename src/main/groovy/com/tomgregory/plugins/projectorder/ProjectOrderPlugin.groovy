package com.tomgregory.plugins.projectorder

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectOrderPlugin implements Plugin<Project> {

    private String NUMERIC_PATTERN = /(\d+).*/

    @Override
    void apply(Project project) {
        ProjectOrderExtension extension = project.getExtensions().create("projectOrder", ProjectOrderExtension.class)

        project.gradle.projectsEvaluated {
            List<Project> sortedSubProjects = sortSubProjects(project)

            Project previousSubProject
            sortedSubProjects.each { subProject ->
                if (previousSubProject) {
                    subProject.tasks[extension.taskName].dependsOn previousSubProject.tasks[extension.taskName]
                }
                previousSubProject = subProject
            }
        }
    }

    private List<Project> sortSubProjects(Project rootProject) {
        return rootProject.rootProject.subprojects.sort { Project subProjectA, Project subProjectB ->
            Optional<Integer> subProjectANumeric = getNumericName(subProjectA)
            Optional<Integer> subProjectBNumeric = getNumericName(subProjectB)

            if (subProjectANumeric.isPresent() && subProjectBNumeric.isPresent()) {
                return subProjectANumeric.get() - subProjectBNumeric.get()
            }
            if (subProjectANumeric.isPresent()) {
                return subProjectANumeric.get()
            }
            if (subProjectBNumeric.isPresent()) {
                return subProjectBNumeric.get()
            }

            return subProjectA.name.compareTo(subProjectB.name)
        }
    }

    private Optional<Integer> getNumericName(Project project) {
        if (isNumericPrefix(project.name)) {
            return Optional.of(extractNumericOrder(project.name))
        }
        return Optional.empty()
    }

    boolean isNumericPrefix(String name) {
        return name.matches(NUMERIC_PATTERN)
    }

    int extractNumericOrder(String name) {
        return Integer.valueOf((name =~ NUMERIC_PATTERN).findAll()[0][1])
    }
}
