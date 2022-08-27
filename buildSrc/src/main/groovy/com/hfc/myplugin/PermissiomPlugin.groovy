package com.hfc.myplugin

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main;
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.Task;
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin

class PermissiomPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }
        project.dependencies {
            implementation 'com.github.hfc123.PermissionRigister:permissions-annotations:v1.0.0'
            implementation 'com.github.hfc123.PermissionRigister:apt-pms-compiler:v1.0.0'
            annotationProcessor 'com.github.hfc123.PermissionRigister:pms-compiler:v1.0.0'
        }
        variants.all { variant ->
            org.gradle.api.tasks.compile.JavaCompile javaCompile = variant.javaCompile
            javaCompile.doLast {
                String[] args = [
                        "-showWeaveInfo",
                        "-1.5",
                        "-inpath", javaCompile.destinationDir.toString(),
                        "-aspectpath", javaCompile.classpath.asPath,
                        "-d", javaCompile.destinationDir.toString(),
                        "-classpath", javaCompile.classpath.asPath,
                        "-bootclasspath", android.bootClasspath.join(File.pathSeparator)
                ]

                MessageHandler handler = new MessageHandler(true);
                new Main().run(args, handler)

                def log = project.logger
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break;
                    }
                }
            }
        }
    }

}