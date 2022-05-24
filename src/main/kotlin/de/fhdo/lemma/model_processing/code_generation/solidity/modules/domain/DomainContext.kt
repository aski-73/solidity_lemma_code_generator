package de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain

import de.fhdo.lemma.data.intermediate.IntermediateDataModel
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.VisitingCodeGenerationHandlerI
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.findCodeGenerationHandlers
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.invokeCodeGenerationHandler
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.SolidityCodeGenerationModule
import de.fhdo.lemma.model_processing.utils.loadModelRoot
import de.fhdo.lemma.model_processing.utils.mainInterface
import de.fhdo.lemma.model_processing.utils.removeFileUri
import net.aveyon.intermediate_solidity.Node
import net.aveyon.intermediate_solidity.SmartContract
import org.eclipse.emf.ecore.EObject
import org.koin.core.KoinComponent
import java.io.File
import java.util.*
import kotlin.reflect.KProperty
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState

internal object DomainContext {

    object State : KoinComponent {
        private const val DOMAIN_SUBFOLDER_NAME = "domain"

        private lateinit var currentIntermediateDomainModelUri: String
        private lateinit var currentIntermediateDomainModelFilePath: String
        private lateinit var currentIntermediateDomainModel: IntermediateDataModel

        private lateinit var currentGeneratedSmartContract: SmartContract

        /**
         * Contains handler classes loaded at runtime
         */
        internal lateinit var domainCodeGenerationHandlers
                : Map<String, Set<Class<VisitingCodeGenerationHandlerI<EObject, Node, Any>>>>

        /**
         * Provide delegated property values for callers
         */
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        operator fun <T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
            val value = when (property.name) {
                "currentDomainPackage" -> currentDomainPackage()
                "currentDomainTargetFolderPath" -> currentDomainTargetFolderPath()
                "resolvedIntermediateDomainModel" -> resolveCurrentIntermediateDomainModel()
                "currentIntermediateDomainModel" -> currentIntermediateDomainModel
                "currentIntermediateDomainModelUri" -> currentIntermediateDomainModelUri
                "currentIntermediateDomainModelFilePath" -> currentIntermediateDomainModelFilePath
                "currentGeneratedSmartContract" -> currentGeneratedSmartContract
                else -> throw IllegalArgumentException("Domain state does not comprise property ${property.name}")
            }

            return value as T
        }

        /**
         * Looks for code generation handlers in the classpath and loads them
         */
        fun initialize() {
            domainCodeGenerationHandlers = findCodeGenerationHandlers<VisitingCodeGenerationHandlerI<EObject, Node, Any>>(
                "${SolidityCodeGenerationModule::class.java.packageName}.domain.handlers"
            )
        }

        /**
         * Set the URI of the current intermediate domain model. Automatically updates the path of the model, too.
         */
        fun setCurrentIntermediateDomainModelUri(uri: String) {
            currentIntermediateDomainModelUri = uri
            currentIntermediateDomainModelFilePath = uri.removeFileUri()
        }

        /**
         * Sets the [SmartContract] that is currently in generation
         */
        fun setCurrentGeneratedSmartContract(sc: SmartContract) {
            currentGeneratedSmartContract = sc
        }

        /**
         * Helper to get the [IntermediateDataModel] instance from the state's [currentIntermediateDomainModelFilePath]
         */
        private fun resolveCurrentIntermediateDomainModel(): IntermediateDataModel {
            currentIntermediateDomainModel = loadModelRoot(currentIntermediateDomainModelFilePath)
            return currentIntermediateDomainModel
        }

        /**
         * Helper to calculate the base target folder path for the current domain code generation run. The path is the
         * base target folder of the current microservice code generation run (hold by the [MainState]) plus a
         * domain-specific sub-folder, currently called "domain".
         */
        private fun currentDomainTargetFolderPath(): String {
            val currentMicroserviceTargetFolderPathForJavaFiles: String by MainState
            return "$currentMicroserviceTargetFolderPathForJavaFiles${File.separator}$DOMAIN_SUBFOLDER_NAME"
        }

        /**
         * Helper to calculate the base package name for the current domain code generation run. This is the base
         * package name of the current microservice code generation run (hold by the [MainState]) plus a domain-specific
         * sub-package, currently called "domain".
         */
        private fun currentDomainPackage(): String {
            val currentMicroservicePackage: String by MainState
            return "$currentMicroservicePackage.$DOMAIN_SUBFOLDER_NAME"
        }
    }

    /**
     * Helper to invoke [VisitingCodeGenerationHandlerI] instances hold by the domain [State] for domain-specific code
     * generation purposes related to the specified [eObject]
     */
    internal fun invokeCodeGenerationHandlers(eObject: EObject): List<Pair<Node, String?>> {
        val handlers = State.domainCodeGenerationHandlers[eObject.mainInterface.name] ?: return emptyList()
        val results = mutableListOf<Pair<Node, String?>>()
        handlers.forEach {
            val handlerResult = invokeCodeGenerationHandler(it.getConstructor().newInstance(), eObject)
            if (handlerResult != null)
                results.add(handlerResult)
        }

        return results
    }
}
