package me.dfournier.versionupgrader

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import me.dfournier.versionupgrader.annotations.Init
import me.dfournier.versionupgrader.annotations.Update
import me.dfournier.versionupgrader.annotations.UpdateSuite
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic


@AutoService(Processor::class)
class VersionUpgraderProcessor : AbstractProcessor() {

    private lateinit var filer: Filer

    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        filer = p0!!.filer
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
                Update::class.java.canonicalName,
                UpdateSuite::class.java.canonicalName,
                Init::class.java.canonicalName
        )
    }


    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null) {
            return false
        }

        processUpdateSuite(roundEnv)
        return true
    }

    private fun processUpdateSuite(roundEnv: RoundEnvironment) {
        val updateMethodList = roundEnv.getElementsAnnotatedWith(Update::class.java)
                .filterIsInstance(ExecutableElement::class.java)
                .filter {
                    checkProcessUpdate(Update::class.java.simpleName, it)
                }

        roundEnv.getElementsAnnotatedWith(UpdateSuite::class.java)
                .filterIsInstance(TypeElement::class.java)
                .filter {
                    checkProcessUpdateSuite(it)
                }
                .forEach { clazz ->
                    // Generate Code
                    val localUpdateMethod =
                            updateMethodList
                                    .filter {
                                        it.enclosingElement == clazz
                                    }
                    generateFile(clazz, localUpdateMethod)
                }

    }

    private fun generateFile(clazz: TypeElement, localUpdateMethod: List<ExecutableElement>) {
        val className = ClassName.get(
                getPackage(clazz).qualifiedName.toString(),
                "Upgrader_" + clazz.simpleName.toString())

        val codeBlock = CodeBlock.builder()

        if (!localUpdateMethod.isEmpty()) {
            codeBlock.addStatement("int currentVersion = getCurrentVersion()")
                    .addStatement("Integer previousVersion = getVersionUpgraderData()")
                    .beginControlFlow("if (previousVersion != null)")
            localUpdateMethod
                    .sortedBy {
                        it.getAnnotation(Update::class.java)
                                .version
                    }
                    .forEach {
                        val version = it.getAnnotation(Update::class.java).version
                        codeBlock.beginControlFlow("if (currentVersion >= \$L && previousVersion < \$L)")
                                .addStatement("\$L()", it.simpleName)
                                .endControlFlow()
                    }
            codeBlock.endControlFlow()


        }
        var methodSpec = MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Execute the update script.")
                .addCode(codeBlock.build())



        val typeSpec = TypeSpec.classBuilder(className.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(clazz.asType()))
                .addMethod(methodSpec.build())

        val file = JavaFile.builder(className.packageName(), typeSpec.build())
                .addFileComment("Generated File, do not modify")
                .build()

        try {
            file.writeTo(filer)
        } catch (e: IOException) {
            logError(
                    clazz,
                    "Unable to write binding for class %s: %s",
                    clazz.simpleName,
                    e.message
            )
        }
    }

    private fun checkProcessUpdate(annotationName: String, element: ExecutableElement): Boolean {
        if (element.modifiers.contains(Modifier.PRIVATE)) {
            logError(
                    element,
                    "The visibility of the method @%s cannot be private",
                    element.simpleName
            )
            return false
        }

        // Verify containing type.
        if (element.enclosingElement.kind != ElementKind.CLASS) {
            logError(
                    element.enclosingElement,
                    "The function %s must be contained in a class",
                    element.simpleName
            )
            return false
        }

        if (element.enclosingElement.getAnnotation(UpdateSuite::class.java) == null) {
            logError(
                    element.enclosingElement,
                    "@%s may only be contained in class annotated with @%s.",
                    annotationName,
                    UpdateSuite::class.java.simpleName
            )
        }
        return true
    }

    private fun checkProcessUpdateSuite(element: TypeElement): Boolean {
        if (element.modifiers.contains(Modifier.PRIVATE)) {
            logError(
                    element,
                    "The class visibility of @%s must not be private",
                    element.simpleName
            )
            return false
        }

        if (element.modifiers.contains(Modifier.ABSTRACT)) {
            logError(
                    element,
                    "The class @%s cannot be abstract.",
                    element.simpleName
            )
            return false
        }

        // Check that the class implements the interface
        var superclass: TypeMirror? = null
        var processedElement = element
        var implementInterface = false
        var loop = true
        do {
            processedElement.interfaces.forEach {
                if (((it as DeclaredType).asElement() as TypeElement).qualifiedName.toString()
                        == Updater::class.java.canonicalName) {
                    implementInterface = true
                }

            }
            superclass = processedElement.superclass
            if (superclass != null && (superclass.kind == ElementKind.CLASS)) {
                processedElement = (superclass as DeclaredType).asElement() as TypeElement
            } else {
                loop = false
            }
        } while (!implementInterface && loop)

        if (!implementInterface) {
            logError(
                    element,
                    "The class @%s must implement the interface %s to use to Version Upgrader",
                    element.simpleName.toString(),
                    Updater::class.java.simpleName
            )
            return false
        }

        return true
    }


    private fun logError(element: Element, message: String, vararg args: Any?) {
        var formatedMessage: String
        if (args.isNotEmpty()) {
            formatedMessage = String.format(message, *args)
        } else {
            formatedMessage = message
        }

        processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                formatedMessage,
                element
        )
    }

    fun getPackage(element: Element): PackageElement {
        var processedElem = element
        while (processedElem.kind != ElementKind.PACKAGE) {
            processedElem = element.enclosingElement
        }
        return processedElem as PackageElement
    }


}