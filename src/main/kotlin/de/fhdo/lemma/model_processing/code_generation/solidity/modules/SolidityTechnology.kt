package de.fhdo.lemma.model_processing.code_generation.solidity.modules

enum class SolidityTechnology(val value: String) {
    ASPECT_STATE_BEHAVIOR("Solidity.StateBehavior"),
    /**
     * One Property of the StateBehavior aspect
     */
    ASPECT_STATE_BEHAVIOR_PLANT_UML("plantuml"),

    ASPECT_PAYABLE("Solidity.Payable"),
    ASPECT_ERROR("Solidity.Error"),

    ASPECT_CONTRACT_TYPE("Solidity.ContractType"),
    ASPECT_CONTRACT_TYPE_IS_CONTRACT("isContract"),
}
