package de.fhdo.lemma.model_processing.code_generation.solidity.modules.services

import de.fhdo.lemma.data.intermediate.IntermediateDataModel
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.VisitingCodeGenerationHandlerI
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.findCodeGenerationHandlers
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.invokeCodeGenerationHandler
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.SolidityCodeGenerationModule
import de.fhdo.lemma.model_processing.code_generation.solidity.simpleName
import de.fhdo.lemma.model_processing.utils.loadModelRoot
import de.fhdo.lemma.model_processing.utils.mainInterface
import de.fhdo.lemma.model_processing.utils.removeFileUri
import net.aveyon.intermediate_solidity.Node
import net.aveyon.intermediate_solidity.SmartContract
import org.eclipse.emf.ecore.EObject
import org.koin.core.KoinComponent
import java.io.File
import java.util.LinkedList
import kotlin.reflect.KProperty
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState

internal object ServicesContext {

    object State : KoinComponent {
        private const val SERVICE_SUBFOLDER_NAME = "services"
        private const val INTERFACE_SUBFOLDER_NAME = "interfaces"

        /**
         * Contains handler classes loaded at runtime
         */
        internal lateinit var servicesCodeGenerationHandlers
                : Map<String, Set<Class<VisitingCodeGenerationHandlerI<EObject, Node, Any>>>>

        /**
         * Provide delegated property values for callers
         */
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        operator fun <T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
            val value = when (property.name) {
                "currentMicroservicePackage" -> currentMicroservicePackage()
                "currentMicroserviceTargetFolderPath" -> currentMicroserviceTargetFolderPath()
                "currentInterfacesGenerationPackage" -> currentInterfacesGenerationPackage()
                "currentInterfacesGenerationTargetFolderPath" -> currentInterfacesGenerationTargetFolderPath()
                else -> throw IllegalArgumentException("Services state does not comprise property ${property.name}")
            }

            return value as T
        }

        fun initialize() {
            servicesCodeGenerationHandlers = findCodeGenerationHandlers(
                "${SolidityCodeGenerationModule::class.java.packageName}.services.handlers"
            )
        }

        /**
         * Helper to build the package of the currently generated microservice
         */
        private fun currentMicroservicePackage() : String {
            val currentMicroservicePackage: String by MainState
            return "$currentMicroservicePackage.${currentMicroserviceQualifiedNameFragment()}"
        }

        /**
         * Helper to build the generation target folder path of the currently generated microservice
         */
        private fun currentMicroserviceTargetFolderPath() : String {
            val currentMicroserviceTargetFolderPathForJavaFiles: String by MainState
            return "$currentMicroserviceTargetFolderPathForJavaFiles${File.separator}" +
                    currentMicroserviceQualifiedNameFragment(File.separator)
        }

        /**
         * Helper to build the name fragment that qualifies the current microservice. It consists of the service's
         * name and version, preceded by the [SERVICE_SUBFOLDER_NAME]
         */
        private fun currentMicroserviceQualifiedNameFragment(separator: String = ".") : String {
            val currentMicroservice = MainState.currentMicroservice
            val microserviceVersion = currentMicroservice.version ?: ""
            val microserviceName = currentMicroservice.simpleName

            val fragmentParts = mutableListOf(SERVICE_SUBFOLDER_NAME)
            fragmentParts.add(microserviceName)
            if (microserviceVersion.isNotEmpty())
                fragmentParts.add(microserviceVersion)

            return fragmentParts.joinToString(separator)
        }

        /**
         * Helper to build the package of the currently generated interface
         */
        private fun currentInterfacesGenerationPackage()
                = "${currentMicroservicePackage()}.$INTERFACE_SUBFOLDER_NAME"

        /**
         * Helper to build the generation target folder path of the currently generated interface
         */
        private fun currentInterfacesGenerationTargetFolderPath()
                ="${currentMicroserviceTargetFolderPath()}${File.separator}$INTERFACE_SUBFOLDER_NAME"
    }

    /**
     * Helper to invoke [VisitingCodeGenerationHandlerI] instances hold by the domain [State] for domain-specific code
     * generation purposes related to the specified [eObject]
     */
    internal fun invokeCodeGenerationHandlers(eObject: EObject): List<Pair<Node, String?>> {
        val handlers = State.servicesCodeGenerationHandlers[eObject.mainInterface.name] ?: return emptyList()
        val results = mutableListOf<Pair<Node, String?>>()
        handlers.forEach {
            val handlerResult = invokeCodeGenerationHandler(it.getConstructor().newInstance(), eObject)
            if (handlerResult != null)
                results.add(handlerResult)
        }

        return results
    }
}
