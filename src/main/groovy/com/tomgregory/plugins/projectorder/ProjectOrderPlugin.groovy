package com.tomgregory.plugins.projectorder

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectOrderPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        ProjectOrderExtension extension = project.getExtensions().create("projectOrder", ProjectOrderExtension.class)

        project.gradle.projectsEvaluated {
            List<Project> sortedSubProjects = sortSubProjects(project)

            Project previousSubProject
            sortedSubProjects.each { subProject ->
                if (previousSubProject) {
                    extension.taskNames.each { taskName ->
                        subProject.tasks[taskName].mustRunAfter previousSubProject.tasks[taskName]
                    }
                }
                previousSubProject = subProject
            }
        }
    }

    private List<Project> sortSubProjects(Project rootProject) {
        return rootProject.subprojects.toSorted(new ProjectComparator())
    }
}
