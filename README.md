A plugin to control ordering of task execution between projects.

##What problem does this plugin solve?

Normally in Gradle you control task execution order by setting a `mustRunAfter` or `dependsOn` relationship. 

You can do this between tasks in the same project:

```groovy
taskA.mustRunAfter taskB
```

 Or between tasks in different projects:

```groovy
findProject('projectA').tasks['myTask'].mustRunAfter findProject('projectB').tasks['myTask']
```

If you need to control task execution order between projects then code such as above is fine for simple use cases.


As the number of projects increases though, the amount of repeated code and therefore maintenance required goes up.
Hence, this plugin was created. 

## Usage

Apply the plugin to your *build.gradle*:
```groovy
plugins {
    id 'com.tomgregory.project-order' version '1.0.0'
}
```
The plugin should only be applied to the parent project. It will inspect all sub-projects of the project it is applied to.

For the plugin to do anything, you need to apply the following configuration in *build.gradle*:

```groovy
projectOrder {
    taskNames = ['taskA', 'taskB']
}
```
**taskNames** is a list of tasks in the sub-projects, for which a *mustRunAfter* relationship should be applied

## An example

Imagine you have a project with multiple sub-projects that each deploy infrastructure resources into the cloud:
```
/my-project
/my-project/networking-resources
/my-project/security-resources
/my-project/compute-resources
```
* each sub-project of *my-project* has a `deploy` task
* the `deploy` tasks must get executed in the correct order, otherwise they will fail. The required order is
`:networking-resources:deploy` then `:security-resources:deploy` then `compute-resources:deploy`  

The *project-order* plugin will set a `mustRunAfter` relationship between the `deploy` task of each sub-project. This will be based
on the ordering it determines.

The project structure can be reordered to control task execution, like this:

```
/my-project
/my-project/1-networking-resources
/my-project/2-security-resources
/my-project/3-compute-resources
```