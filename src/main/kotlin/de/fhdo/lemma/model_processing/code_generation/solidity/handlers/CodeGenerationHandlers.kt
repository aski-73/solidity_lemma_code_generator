package de.fhdo.lemma.model_processing.code_generation.solidity.handlers

import de.fhdo.lemma.data.intermediate.IntermediateImportedAspect
import de.fhdo.lemma.model_processing.code_generation.solidity.findAndMapAnnotatedClassesWithInterface
import net.aveyon.intermediate_solidity.Node
import org.eclipse.emf.ecore.EObject

/*
 * This file is based on
 * https://github.com/SeelabFhdo/lemma/blob/main/code%20generators/de.fhdo.lemma.model_processing.code_generation.java_base/src/main/kotlin/de/fhdo/lemma/model_processing/code_generation/java_base/handlers/CodeGenerationHandlers.kt
 */

/**
 * Concrete Code Generation Handler must be annotated with this annotation
 */
@Target(AnnotationTarget.CLASS)
annotation class CodeGenerationHandler

/**
 * Base interface for code generation handlers (cf. [CodeGenerationHandler]).
 */
interface CodeGenerationHandlerI<T : EObject, N : Node, C : Any> {
    /**
     * The [EObject] class, for which this handler can generate an AST [Node].
     */
    fun handlesEObjectsOfInstance(): Class<T>

    /**
     * The [Node] class that this handler generates
     */
    fun generatesNodesOfInstance(): Class<N>

    /**
     * Get aspects from an [EObject] instance. The handler must implement this function in order to enable subsequent
     * invocation of [AspectHandlerI] realizations.
     */
    fun getAspects(eObject: T): List<IntermediateImportedAspect> = emptyList()

    /**
     * Function which needs to be realized by concrete handlers. The context is an optional information that may be
     * passed to a handler, e.g., the previously generated parent [Node] of the yet-to-generate [Node].
     *
     * A code generation handler can either return null (i.e., no AST [Node] was generated/reified at all for the given
     * [EObject] and context), or a pair that holds the generated [Node] and possibly a target file path. If no target
     * file path is returned, a generated [Node] will not be serialized in case the handler is a
     * [VisitingCodeGenerationHandlerI].
     */
    fun execute(eObject: T, context: C? = null): Pair<N, String?>?
}

/**
 * Invoke the given code generation [handler].
 *
 * @author [Florian Rademacher](mailto:florian.rademacher@fh-dortmund.de)
 */
internal fun <T : EObject, N : Node, C : Any> invokeCodeGenerationHandler(
    handler: CodeGenerationHandlerI<T, N, C>,
    eObject: T,
    context: C? = null
): Pair<N, String?>? {
//    executeCodeGenerationHandlerPreActions(eObject)
    val result = handler.execute(eObject, context) ?: return null
    val (generatedNode, generatedFilePath) = result
//    val adaptedNode = handler.executeCodeGenerationHandlerPostActions(eObject, generatedNode, context)
    return generatedNode to generatedFilePath
}

/**
 * Helper to find [CodeGenerationHandlerI] implementations, possibly in other JAR archives by providing the respective
 * [classLoaders].
 */
internal inline fun <reified T : CodeGenerationHandlerI<*, *, *>> findCodeGenerationHandlers(
    searchPackage: String,
    vararg classLoaders: ClassLoader
) = findAndMapAnnotatedClassesWithInterface<T>(
    searchPackage,
    CodeGenerationHandler::class.qualifiedName!!,
    *classLoaders
)
{ it.handlesEObjectsOfInstance().name }
