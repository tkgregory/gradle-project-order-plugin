package com.tomgregory.plugins.projectorder

import org.gradle.api.Project

class ProjectComparator implements Comparator<Project> {

    private String NUMERIC_PATTERN = /(\d+).*/

    @Override
    int compare(Project projectA, Project projectB) {
        Optional<Integer> projectANumeric = getNumericName(projectA)
        Optional<Integer> projectBNumeric = getNumericName(projectB)

        if (projectANumeric.isPresent() && projectBNumeric.isPresent()) {
            int numericComparison = projectANumeric.get() - projectBNumeric.get()
            if (numericComparison == 0) {
                return simpleNameComparison(projectA, projectB)
            }
            return numericComparison
        }
        if (projectANumeric.isPresent()) {
            return -1
        }
        if (projectBNumeric.isPresent()) {
            return 1
        }

        return simpleNameComparison(projectA, projectB)
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

    private static int simpleNameComparison(Project projectA, Project projectB) {
        projectA.name.compareTo(projectB.name)
    }
}