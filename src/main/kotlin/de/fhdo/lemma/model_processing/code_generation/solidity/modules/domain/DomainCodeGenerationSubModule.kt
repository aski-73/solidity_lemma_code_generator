package de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain

import de.fhdo.lemma.data.intermediate.IntermediateDataModel
import de.fhdo.lemma.model_processing.absoluteBasePath
import de.fhdo.lemma.model_processing.code_generation.solidity.serializers.CodeGenerationSerializerI
import de.fhdo.lemma.model_processing.code_generation.solidity.serializers.ExtendedGenerationGapSerializer
import de.fhdo.lemma.model_processing.utils.filterByType
import de.fhdo.lemma.model_processing.utils.loadModelRoot
import de.fhdo.lemma.model_processing.utils.removeFileUri
import de.fhdo.lemma.service.ImportType
import de.fhdo.lemma.service.intermediate.IntermediateServiceModel
import de.fhdo.lemma.utils.LemmaUtils
import net.aveyon.intermediate_solidity.SmartContractModel
import org.eclipse.emf.ecore.EObject
import org.koin.core.KoinComponent
import org.koin.core.inject
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.DomainContext.State as DomainState

internal class DomainCodeGenerationSubModule : KoinComponent {
    companion object {
        /**
         * Static invocation function for external callers
         */
        fun invoke() {
            DomainCodeGenerationSubModule().execute()
        }
    }

    /**
     * Execute the sub-module's logic
     */
    private fun execute() {
        // Find code generation handlers
        DomainState.initialize()

        /**
         * Load Domain Model with the mapped StateBehavior aspect by the import uri provided in the
         * Intermediate Service Model.
         */
        val intermediateServiceModelResource: IntermediateServiceModel by MainState

        val allDomainModelUris = resolveImportedDomainModelUrisTransitively(intermediateServiceModelResource)
        allDomainModelUris.forEach { uri ->
            DomainState.setCurrentIntermediateDomainModelUri(uri)
            val resolvedIntermediateDomainModel: IntermediateDataModel by DomainState
            invokeCodeGenerationHandlers(resolvedIntermediateDomainModel)

            resolvedIntermediateDomainModel.eAllContents().forEach { invokeCodeGenerationHandlers(it) }
        }
        invokeSerializers()
    }

    /**
     * Helper to get all URIs of domain models being imported by the given service model and, transitively, of all
     * domain models being imported by previously resolved domain models. Relative URIs will be converted to absolute
     * URIs based on their importing resource's path. Consequently, all returned URIs are absolute.
     *
     * In order to choose the right import uri (because it can contain multiple) we choose those whose
     * property "Import Type Name" has the value "DATATYPES" because these are neither Microservice nor
     * Technology imports
     */
    private fun resolveImportedDomainModelUrisTransitively(startModel: IntermediateServiceModel): Set<String> {
        val resolvedModelUris = mutableSetOf<String>()
        val urisTodo = ArrayDeque<Pair<String, String>>()
        val startModelAbsolutePath = startModel.eResource().absoluteBasePath()
        startModel.imports.filterByType(ImportType.DATATYPES).forEach {
            urisTodo.add(it.importUri to startModelAbsolutePath)
        }

        while (urisTodo.isNotEmpty()) {
            val (currentUri, importingModelAbsolutePath) = urisTodo.removeLast()
            val currentPath = currentUri.removeFileUri()
            val absoluteUri = LemmaUtils.convertToAbsoluteFileUri(currentPath, importingModelAbsolutePath)
            resolvedModelUris.add(absoluteUri)

            val modelRoot = loadModelRoot<IntermediateDataModel>(absoluteUri.removeFileUri())
            modelRoot.imports.filterByType(ImportType.DATATYPES).forEach {
                urisTodo.add(it.importUri to absoluteUri.removeFileUri())
            }
        }

        return resolvedModelUris
    }

    /**
     * Trigger invocation of visiting code generation handlers on [EObject].
     */
    private fun invokeCodeGenerationHandlers(eObject: EObject) {
        // Ignore the return values since the code gen handlers write directly into the MainState.
        // The code generation handlers build a concrete instance of intermediate solidity
        DomainContext.invokeCodeGenerationHandlers(eObject)
    }

    /**
     * Calls the code serializers which ultimatly produce the solidity code
     */
    private fun invokeSerializers() {
        val currentIntermediateDomainModel: IntermediateDataModel by DomainState
        val originalModelPath = currentIntermediateDomainModel.sourceModelUri.removeFileUri()
        val targetFolderPath: String by MainState

        val generatedSmartContractModels: List<SmartContractModel> by MainState
        // Concrete code is generated by the serializer
        val serializer: CodeGenerationSerializerI = ExtendedGenerationGapSerializer()

        for (smartContractModel in generatedSmartContractModels) {

            val generatedFileContents = serializer.serialize(
                smartContractModel,
                targetFolderPath,
                targetFolderPath,
                null,
                originalModelPath
            )

            generatedFileContents.forEach { (targetFilePath, generationResult) ->
                // TODO
            }
        }
    }
}
