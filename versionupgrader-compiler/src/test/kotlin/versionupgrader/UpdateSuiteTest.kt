package versionupgrader

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import me.dfournier.versionupgrader.VersionUpgraderProcessor
import org.junit.Test

class UpdateSuiteTest {

    @Test
    fun testInterfaceNotImplemented() {

        val javaFileObject = JavaFileObjects.forSourceLines(
                "foo.bar.A",
                "package me.dfournier.versionupgrader;",
                "",
                "import me.dfournier.versionupgrader.annotations.UpdateSuite;",
                "",
                "@UpdateSuite",
                "public class A {",
                "}"
        )

        val compilation = javac().withProcessors(VersionUpgraderProcessor())
                .compile(javaFileObject)
        assertThat(compilation)
                .failed()
        assertThat(compilation)
                .hadErrorContaining("The class @A must implement the interface Updater to use to Version Upgrader")

    }

    @Test
    fun simple() {
        val compilation = javac().withProcessors(VersionUpgraderProcessor())
                .compile(JavaFileObjects.forResource("EmptyTestSuite/A.java"))
        assertThat(compilation)
                .generatedSourceFile("me.dfournier.versionupgrader.Upgrader_A")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("EmptyTestSuite/Upgrader_A.java"))
    }

}