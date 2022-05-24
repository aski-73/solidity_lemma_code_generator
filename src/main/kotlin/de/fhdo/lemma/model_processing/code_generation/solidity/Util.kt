package de.fhdo.lemma.model_processing.code_generation.solidity

import de.fhdo.lemma.model_processing.utils.mainInterface
import de.fhdo.lemma.model_processing.utils.putValue
import de.fhdo.lemma.service.intermediate.IntermediateInterface
import de.fhdo.lemma.service.intermediate.IntermediateMicroservice
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.eclipse.emf.ecore.EObject

/**
 * Property representing the simple name of an [EObject], e.g., an [IntermediateMicroservice] or
 * [IntermediateInterface].
 *
 * @author [Florian Rademacher](mailto:florian.rademacher@fh-dortmund.de)
 */
val EObject.simpleName
    get() = when (this) {
        is IntermediateMicroservice -> name.substringAfterLast(".")
        is IntermediateInterface -> name.substringAfterLast(".")
        else -> throw IllegalArgumentException("EObject of type ${this.mainInterface.name} does not have a simple name")
    }

/**
 * Helper to find annotated classes, possibly within other JAR archives leveraging the given [classLoaders].
 *
 * @author [Florian Rademacher](mailto:florian.rademacher@fh-dortmund.de)
 */
internal fun findAnnotatedClasses(
    searchPackage: String, annotation: String, vararg classLoaders: ClassLoader,
    filter: (ClassInfo) -> Boolean
): List<ClassInfo> {
    val classgraph = ClassGraph()
        .enableClassInfo()
        .enableAnnotationInfo()
        .acceptPackages(searchPackage)

    if (classLoaders.isNotEmpty())
        classgraph.overrideClassLoaders(*classLoaders)

    return classgraph
        .scan()
        .getClassesWithAnnotation(annotation)
        .filter { filter(it) }
        .toList()
}

/**
 * Helper to find annotated classes that also implement a certain interface. When explicitly specifying [classLoaders]
 * other JAR archives than the current one might be searched.
 *
 * @author [Florian Rademacher](mailto:florian.rademacher@fh-dortmund.de)
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> findAnnotatedClassesWithInterface(
    searchPackage: String, annotation: String,
    vararg classLoaders: ClassLoader, interfaceClassname: String = T::class.java.name
) = findAnnotatedClasses(
    searchPackage,
    annotation,
    *classLoaders
)
{ it.implementsInterface(interfaceClassname) }  // Interface filter
    .map { it.loadClass() as Class<T> }

/**
 * Helper to find annotated classes that also implement a certain interface. When explicitly specifying [classLoaders]
 * other JAR archives than the current one might be searched. The result of the function will be a map that assigns the
 * identifier string produced by the specified [identifierBuilder] to the found classes.
 *
 * @author [Florian Rademacher](mailto:florian.rademacher@fh-dortmund.de)
 */
internal inline fun <reified T> findAndMapAnnotatedClassesWithInterface(
    searchPackage: String, annotation: String,
    vararg classLoaders: ClassLoader, interfaceClassname: String = T::class.java.name,
    identifierBuilder: (T) -> String
): Map<String, Set<Class<T>>> {
    val foundClasses = findAnnotatedClassesWithInterface<T>(
        searchPackage, annotation, *classLoaders,
        interfaceClassname = interfaceClassname
    )

    val resultMap = mutableMapOf<String, MutableSet<Class<T>>>()
    foundClasses.forEach {
        val identifier = identifierBuilder(it.getDeclaredConstructor().newInstance())
        resultMap.putValue(identifier, it)
    }

    return resultMap
}
