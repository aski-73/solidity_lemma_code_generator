package de.fhdo.lemma.model_processing.code_generation.solidity.modules.services

import de.fhdo.lemma.service.intermediate.IntermediateServiceModel
import org.eclipse.emf.ecore.EObject
import org.koin.core.KoinComponent
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.services.ServicesContext.State as ServicesState

internal class ServicesCodeGenerationSubModule : KoinComponent {
    companion object {
        /**
         * Static invocation function for external callers
         */
        fun invoke() {
            ServicesCodeGenerationSubModule().execute()
        }
    }

    /**
     * Execute the sub-module's logic
     */
    private fun execute() {
        ServicesState.initialize()

        /**
         * Load Domain Model with the mapped StateBehavior aspect by the import uri provided in the
         * Intermediate Service Model.
         */
        val intermediateServiceModelResource: IntermediateServiceModel by MainState

        intermediateServiceModelResource.eAllContents().forEach {
            invokeCodeGenerationHandler(it)
        }
    }

    /**
     * Trigger invocation of visiting code generation handlers on [EObject]. This will also lead to the invocation of
     * subsequent actions, e.g., aspect handlers and Genlet-specific code generation handlers (cf. the
     * executeSubActions() function in
     * [de.fhdo.lemma.model_processing.code_generation.java_base.handlers.CodeGenerationHandlerI]).
     */
    private fun invokeCodeGenerationHandler(eObject: EObject) {
//        val currentDomainTargetFolderPath: String by DomainState
//        val currentIntermediateDomainModel: IntermediateDataModel by DomainState
//        val originalModelPath = currentIntermediateDomainModel.sourceModelUri.removeFileUri()
//        val serializer: CodeGenerationSerializerI by inject()
//
        val generatedNodesAndTargetFiles = ServicesContext.invokeCodeGenerationHandlers(eObject)
//        for((generatedNode, targetFile) in generatedNodesAndTargetFiles) {
//            if (targetFile == null)
//                continue
//
//            val generatedFileContents = serializer.serialize(
//                generatedNode,
//                currentDomainTargetFolderPath,
//                targetFile,
//                eObject,
//                originalModelPath
//            )
//
//            generatedFileContents.forEach { (targetFilePath, generationResult) ->
//                val (generatedContent, _) = generationResult
//                MainState.addGeneratedFileContent(generatedContent, targetFilePath)
//            }
//        }
    }
}
