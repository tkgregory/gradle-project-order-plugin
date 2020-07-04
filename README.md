A Gradle plugin to control ordering of task execution between projects.

## What problem does this plugin solve?

Normally in Gradle you **control task execution order** by setting a `mustRunAfter` relationship. 

You can do this between tasks in the same project:

```groovy
taskA.mustRunAfter taskB
```

 Or between tasks in different projects:

```groovy
findProject('projectA').tasks['myTask'].mustRunAfter findProject('projectB').tasks['myTask']
```

If you need to control task execution order *between projects* then the above code is OK for simple use cases.

As the number of projects increases though, the amount of **repeated code and maintenance required goes up**. Hence, the Project Order Plugin was created. 

## Usage

Apply the Project Order Plugin to your *build.gradle*:
```groovy
plugins {
    id 'com.tomgregory.project-order' version '1.0.0'
}
```
The plugin should only be applied to a single parent project. It will inspect all subprojects of the project it is applied to.

For the plugin to do anything, you need to apply the following configuration in *build.gradle*:

```groovy
projectOrder {
    taskNames = ['taskA', 'taskB']
}
```
* **taskNames** is a list of tasks in the subprojects, for which a *mustRunAfter* relationship should be applied

## Ordering

The Project Order Plugin uses the following rules to determine the order in which subpproject tasks should be linked with a `mustRunAfter` relationship:
1. A subproject with a numeric prefix comes before one with a letter prefix i.e. *66-project* before *abc-project*
1. Subprojects with a numeric prefix are sorted numerically i.e. *9-project* before *66-project*
1. Subprojects with a letter prefix are sorted alphabetically i.e. *cat-project* before *dog-project*

For more details of ordering and some examples, see [ProjectComparatorTest](src/test/groovy/com/tomgregory/plugins/projectorder/ProjectComparatorTest.groovy).

## An example

Imagine you have a project with multiple subprojects that each deploy infrastructure resources into the cloud:
```
/my-project
/my-project/networking-resources
/my-project/security-resources
/my-project/compute-resources
```
* each subproject of *my-project* has a `deploy` task
* the `deploy` tasks must get executed in the correct order, otherwise they will fail. The required order is
`:networking-resources:deploy` then `:security-resources:deploy` then `compute-resources:deploy`  

The Project Order Plugin will set a `mustRunAfter` relationship between the `deploy` task of each subproject. This will be based
on the ordering as defined above.

For the plugin to work properly the projects should be renamed like this:
```
/my-project
/my-project/1-networking-resources
/my-project/2-security-resources
/my-project/3-compute-resources
```

Now this code just needs to be applied to *build.gradle*:
```groovy
id 'com.tomgregory.project-order' version '1.0.0'

projectOrder {
    taskNames = ['deploy']
}
```

This same example is executed in [StandaloneExampleTest](src/test/groovy/com/tomgregory/plugins/projectorder/StandaloneExampleTest.groovy).