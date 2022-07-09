package de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain

import de.fhdo.lemma.data.intermediate.IntermediateDataModel
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.*
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.findCodeGenerationHandlers
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.SolidityCodeGenerationModule
import de.fhdo.lemma.model_processing.utils.loadModelRoot
import de.fhdo.lemma.model_processing.utils.mainInterface
import de.fhdo.lemma.model_processing.utils.removeFileUri
import net.aveyon.intermediate_solidity.Node
import net.aveyon.intermediate_solidity.SmartContract
import org.eclipse.emf.ecore.EObject
import org.koin.core.KoinComponent
import java.io.File
import kotlin.reflect.KProperty
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState

internal object DomainContext {

    object State : KoinComponent {
        private const val DOMAIN_SUBFOLDER_NAME = "domain"

        private lateinit var currentIntermediateDomainModel: IntermediateDataModel
        private lateinit var currentGeneratedSmartContract: SmartContract

        /**
         * Contains handler classes loaded at runtime
         */
        internal lateinit var domainCodeGenerationHandlers
                : Map<String, Set<Class<CodeGenerationHandlerI<EObject, Node, Any>>>>

        /**
         * Provide delegated property values for callers
         */
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        operator fun <T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
            val value = when (property.name) {
                "currentDomainPackage" -> currentDomainPackage()
                "currentDomainTargetFolderPath" -> currentDomainTargetFolderPath()
                "currentIntermediateDomainModel" -> currentIntermediateDomainModel
                "currentGeneratedSmartContract" -> currentGeneratedSmartContract
                else -> throw IllegalArgumentException("Domain state does not comprise property ${property.name}")
            }

            return value as T
        }

        /**
         * Looks for code generation handlers in the classpath and loads them
         */
        fun initialize() {
            domainCodeGenerationHandlers = findCodeGenerationHandlers(
                "${SolidityCodeGenerationModule::class.java.packageName}.domain.handlers"
            )
        }

        /**
         * Sets the [SmartContract] that is currently in generation
         */
        fun setCurrentGeneratedSmartContract(sc: SmartContract) {
            currentGeneratedSmartContract = sc
        }

        /**
         * Helper to get the [IntermediateDataModel] instance from an URI
         */
        fun resolveCurrentIntermediateDomainModel(intermediateDomainModelFileUri: String): IntermediateDataModel {
            val currentIntermediateDomainModelFilePath = intermediateDomainModelFileUri.removeFileUri()

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
}
