package com.tomgregory.plugins.projectorder

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProjectOrderPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        ProjectOrderExtension extension = project.getExtensions().create("projectOrder", ProjectOrderExtension.class)

        project.gradle.projectsEvaluated {
            List<Project> sortedSubprojects = sortSubprojects(project)

            Project previousSubproject
            sortedSubprojects.each { subproject ->
                if (previousSubproject) {
                    extension.taskNames.each { taskName ->
                        def subprojectTask = subproject.tasks.findByName(taskName)
                        def previousSubprojectTask = previousSubproject.tasks.findByName(taskName)
                        if (subprojectTask && previousSubprojectTask) {
                            subprojectTask.mustRunAfter previousSubprojectTask
                        }
                    }
                }
                previousSubproject = subproject
            }
        }
    }

    private static List<Project> sortSubprojects(Project rootProject) {
        return rootProject.subprojects.toSorted(new ProjectComparator())
    }
}
