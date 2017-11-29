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

        val javaFileObject = JavaFileObjects.forSourceLines(
                "foo.bar.A",
                "package me.dfournier.versionupgrader;",
                        "",
                        "import me.dfournier.versionupgrader.annotations.UpdateSuite;",
                        "import org.jetbrains.annotations.Nullable;",
                        "",
                        "@UpdateSuite",
                        "public class A implements Updater {",
                        "    @Override",
                        "    public int getCurrentVersion() {",
                        "        return 0;",
                        "    }",
                        "",
                        "    @Nullable",
                        "    @Override",
                        "    public Integer getVersionUpgraderData() {",
                        "        return null;",
                        "    }",
                        "",
                        "    @Override",
                        "    public void setVersionUpgraderData(int data) {",
                        "",
                        "    }",
                        "}"
        )
        val expectedQualifiedName = "me.dfournier.versionupgrader.Upgrader_A"
        val expectedOutput = JavaFileObjects.forSourceLines(
                expectedQualifiedName,
                ""
        )


        val compilation = javac().withProcessors(VersionUpgraderProcessor())
                .compile(javaFileObject)
        assertThat(compilation)
                .generatedSourceFile(expectedQualifiedName)
                .hasSourceEquivalentTo(expectedOutput)

    }

}