package de.fhdo.lemma.model_processing.code_generation.solidity.modules

import de.fhdo.lemma.model_processing.builtin_phases.code_generation.AbstractCodeGenerationModule
import org.koin.core.KoinComponent
import java.nio.charset.Charset

/**
 * Base template for main code generation modules.
 */
internal abstract class CodeGenerationModuleBase<E: Any> : AbstractCodeGenerationModule(), KoinComponent {
    /**
     * Callback to initialize the code generation state
     */
    abstract fun initializeState(moduleArguments: Array<String>)

    /**
     * Callback for the retrieval of relevant code generation elements
     */
    abstract fun getGenerationElement() : E

    /**
     * Callback for the actual code generation from a single code generation element
     */
    abstract fun generateCode(element: E)

    /**
     * This method performs the actual code generation. The only requirement posed by LEMMA's
     * model processing framework is that the[AbstractCodeGenerationModule.execute]implementation of a code generation module returns a map with entries as
     * follows: - Key: Path of a generated file. By default, the file must reside in
     * the folder passed to the model processor in the "--target_model" commandline
     * option (see below). - Value: A [Pair] instance, whose first
     * component contains the generated file's content and whose second component
     * identifies the content's [Charset]. From the map
     * entries, LEMMA's code generation framework will write the generated files to
     * the filesystem of the model processor user.
     */
    override fun execute(phaseArguments: Array<String>, moduleArguments: Array<String>)
            : Map<String, Pair<String, Charset>> {

        /* Initialize state */
        initializeState(moduleArguments)

        /* Generate code, and serialize dependencies and property files per code generation element */
        val element = getGenerationElement()
        generateCode(element)

        val generatedFileContents: Map<String, Pair<String, Charset>> = HashMap()

        /*
         * Return the map of generated file contents and their charsets per path for the model processor framework to
         * write the files to disk
         */
        return generatedFileContents
    }
}
