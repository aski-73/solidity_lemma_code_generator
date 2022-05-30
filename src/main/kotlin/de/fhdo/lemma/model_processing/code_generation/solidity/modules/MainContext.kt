package de.fhdo.lemma.model_processing.code_generation.solidity.modules

import de.fhdo.lemma.model_processing.utils.modelRoot
import de.fhdo.lemma.service.intermediate.IntermediateMicroservice
import de.fhdo.lemma.service.intermediate.IntermediateServiceModel
import net.aveyon.intermediate_solidity.SmartContractModel
import org.koin.core.KoinComponent
import org.eclipse.emf.ecore.resource.Resource
import java.util.LinkedList
import kotlin.reflect.KProperty

internal class MainContext {
    /**
     * State as singleton
     */
    object State : KoinComponent {
        private lateinit var modelFilePath: String
        internal lateinit var model: IntermediateServiceModel
        private lateinit var targetFolderPath: String

        private lateinit var currentGeneratedSmartContractModel: SmartContractModel

        private var generatedSmartContractModels: MutableList<SmartContractModel> = LinkedList()

        lateinit var currentMicroservice: IntermediateMicroservice
            private set

        internal val meivsm: net.aveyon.meivsm.Api = net.aveyon.meivsm.App()

        var license: String = "GPL-3.0"

        var pragma: String = "<0.9.0"

        /**
         * Initialize the state of the context
         */
        fun initialize(
            intermediateServiceModelFilePath: String,
            intermediateServiceModelResource: Resource,
            targetFolderPath: String
        ) {
            modelFilePath = intermediateServiceModelFilePath
            model = intermediateServiceModelResource.modelRoot()
            this.targetFolderPath = targetFolderPath
        }

        fun setCurrentGeneratedSmartContractModel(scm: SmartContractModel) {
            currentGeneratedSmartContractModel = scm

            generatedSmartContractModels.add(scm)
        }

        /**
         * Provide delegated property values back to callers
         */
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        operator fun <T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
            val value = when (property.name) {
                "mappingModel", "intermediateServiceModelResource" -> model
                "currentGeneratedSmartContractModel" -> currentGeneratedSmartContractModel
                "generatedSmartContractModels" -> generatedSmartContractModels
                "targetFolderPath" -> targetFolderPath
                else -> throw IllegalArgumentException("Main state does not comprise property ${property.name}")
            }

            return value as T
        }
    }
}
