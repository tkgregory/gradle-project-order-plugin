package com.tomgregory.plugins.projectorder

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

class TestUtil {
    static BuildResult runTasks(File projectDir, String... taskNames) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(taskNames)
                .withPluginClasspath()
                .withDebug(true)
                .build()
    }

    static List<String> executedTaskPaths(BuildResult buildResult) {
        return buildResult.tasks.collect { element -> element.path }
    }
}