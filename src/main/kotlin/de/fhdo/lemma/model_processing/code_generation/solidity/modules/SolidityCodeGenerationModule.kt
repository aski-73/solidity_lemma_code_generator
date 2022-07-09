package de.fhdo.lemma.model_processing.code_generation.solidity.modules

import de.fhdo.lemma.model_processing.annotations.CodeGenerationModule
import de.fhdo.lemma.model_processing.builtin_phases.code_generation.AbstractCodeGenerationModule
import de.fhdo.lemma.model_processing.code_generation.solidity.languages.INTERMEDIATE_SERVICE_MODEL_LANGUAGE_DESCRIPTION
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.DomainCodeGenerationSubModule
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.services.ServicesCodeGenerationSubModule
import de.fhdo.lemma.service.intermediate.IntermediateServiceModel
import net.aveyon.intermediate_solidity.SmartContractModel
import net.aveyon.intermediate_solidity.impl.SmartContractImpl
import net.aveyon.intermediate_solidity.impl.SmartContractModelImpl
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState

/**
 * LEMMA's model processing framework supports model-based structuring of code
 * generators. This class implements a code generation module as expected by the
 * framework, i.e., the class receives the [CodeGenerationModule] annotation and extends [AbstractCodeGenerationModule].
 */
@CodeGenerationModule(name = "main")
internal class SolidityCodeGenerationModule : CodeGenerationModuleBase<IntermediateServiceModel>() {
    override fun initializeState(moduleArguments: Array<String>) {
        /* Initialize the main state hold by the main context */
        MainState.initialize(
            resource,
            if (targetFolder.endsWith("/")) targetFolder  else "$targetFolder/"
        )
    }

    override fun getGenerationElement(): IntermediateServiceModel {
        return MainState.model
    }

    override fun generateCode(element: IntermediateServiceModel) {
        println("starting generation")

        /*
         * Delegate population of the smart contract model to the domain code generation sub module,
         * since operations and structures are defined there.
         *
         * The DomainCodeGenerationSubModule  creates a Smart Contract instance for every with StateBehavior annotated DataStructure.
         * The Smart Contracts are contained inside of a SmartContractModel instance. A SmartContractModel instance
         * ServicesCodeGenerationSubModule.invoke();
         */
        DomainCodeGenerationSubModule.invoke();
    }

    /**
     * Return the language namespace for the intermediate model kind with which this code generator can deal, i.e.,
     * intermediate service models
     */
    override fun getLanguageNamespace() = INTERMEDIATE_SERVICE_MODEL_LANGUAGE_DESCRIPTION.nsUri
}
