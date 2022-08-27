package com.hfc.myplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class TestPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task('pluginTest') {
            doLast {
                println 'Hello World'
            }
        }
    }
}