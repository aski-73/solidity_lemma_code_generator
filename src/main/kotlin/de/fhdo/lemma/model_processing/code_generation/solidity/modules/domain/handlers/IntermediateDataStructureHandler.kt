package de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.handlers

import de.fhdo.lemma.data.ComplexTypeFeature
import de.fhdo.lemma.data.intermediate.IntermediateDataStructure
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.CodeGenerationHandler
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.VisitingCodeGenerationHandlerI
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.SolidityTechnology
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.handlers.GenerationUtil.Companion.mapToError
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.handlers.GenerationUtil.Companion.mapToEvent
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.handlers.GenerationUtil.Companion.mapToSmartContract
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.handlers.GenerationUtil.Companion.mapToStructure
import net.aveyon.intermediate_solidity.*
import net.aveyon.intermediate_solidity.impl.ExpressionStringImpl
import net.aveyon.intermediate_solidity.impl.SmartContractModelImpl
import net.aveyon.meivsm.MetaInformation
import org.antlr.stringtemplate.language.Expr
import java.io.File
import java.io.FileInputStream
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.DomainContext.State as DomainState
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState

/**
 * Handles code generation of [IntermediateDataStructure]s that are annotated (by using the Mapping DSL) with
 * 'StateBehavior' and those without the annotation.
 * The first will be mapped to a [SmartContract] instance.
 * The latter to a [Structure] instance residing in a previously created [SmartContract] instance.
 *
 * This approach limits the smart contract generation as follows:
 * 1. A DML model is only allowed to have one mapped data structure (annotated data structure), since
 * it's not possible to assign normal structures to more than one mapped data structure.
 */
@CodeGenerationHandler
class IntermediateDataStructureHandler : VisitingCodeGenerationHandlerI<IntermediateDataStructure, Node, Any> {
    override fun handlesEObjectsOfInstance(): Class<IntermediateDataStructure> = IntermediateDataStructure::class.java

    override fun generatesNodesOfInstance(): Class<Node> = Node::class.java

    override fun getAspects(eObject: IntermediateDataStructure) = eObject.aspects!!

    override fun execute(eObject: IntermediateDataStructure, context: Any?): Pair<Node, String?> {
        val isContract = eObject.aspects
            .find { it.qualifiedName.equals(SolidityTechnology.ASPECT_CONTRACT_TYPE.value) }
            ?.propertyValues?.any {
                it.value == "true" && it.property.name == SolidityTechnology.ASPECT_CONTRACT_TYPE_IS_CONTRACT.value
            }
        val isError = eObject.featureNames.any { it.equals(ComplexTypeFeature.DOMAIN_EVENT.name) }
                && eObject.aspects.any { it.qualifiedName.equals(SolidityTechnology.ASPECT_ERROR.value) }
        // Need to include isError in order not to set both to true (both contain the event feature)
        val isEvent = !isError && eObject.featureNames.any { it.equals(ComplexTypeFeature.DOMAIN_EVENT.name) }

        val currentGeneratedSmartContract: SmartContract by DomainState

        return if (isContract == true) {
            val sc = mapToSmartContract(eObject)
            DomainState.setCurrentGeneratedSmartContract(sc)
            val currentGeneratedSmartContractModel: SmartContractModel by MainState
            currentGeneratedSmartContractModel.definitions.contracts.add(sc)

            // Not every contract has a StateBehavior Aspect, but for those who have we parse the Behavior described
            // in plant uml
            val model = parseStateBehavior(eObject)
            if (model != null) {
                insertMeivsmContractConcepts(model.first, sc)
                optimizeContract(sc, model.second)
            }

            Pair(sc, null)
        } else if (isEvent) {
            val event = mapToEvent(eObject)
            currentGeneratedSmartContract.definitions.events.add(event)
            Pair(event, null)
        } else if (isError) {
            val event = mapToError(eObject)
            currentGeneratedSmartContract.definitions.errors.add(event)
            Pair(event, null)
        } else { // All "normal" structures are added as Solidity structs into the last created SmartContract instance
            val struct = mapToStructure(eObject)
            currentGeneratedSmartContract.definitions.structures.add(struct)
            Pair(struct, null)
        }
    }

    /**
     * Parses the plant uml file defined in the State Behavior aspect
     */
    private fun parseStateBehavior(eObject: IntermediateDataStructure)
            : net.aveyon.intermediate_solidity.util.Pair<SmartContractModel, MetaInformation>? {

        val stateBehavior = eObject.aspects
            .find { it.qualifiedName == SolidityTechnology.ASPECT_STATE_BEHAVIOR.value }
            ?.propertyValues?.find { it.property.name == SolidityTechnology.ASPECT_STATE_BEHAVIOR_PLANT_UML.value }

        if (stateBehavior != null) {
            // Path to file
            val file = File(stateBehavior.value)
            if (file.exists()) {
                val fileIs = FileInputStream(stateBehavior.value)
                return fileIs.use {
                    MainState.meivsm.parse(it)
                }
            }
        }

        return null
    }

    private fun insertMeivsmContractConcepts(meivsmContract: SmartContractModel, contract: SmartContract) {

        // If parsing if successful, only one contract exists in the meivsm SmartContractModel
        val meivsmContracts = meivsmContract.definitions.contracts
            .filter { it.name == contract.name }

        if (meivsmContracts.isNotEmpty()) {
            // Meivsm generates fields and one function
            meivsmContracts[0]
                .fields.forEach {
                    contract.fields.add(it)
                }

            meivsmContracts[0]
                .definitions.functions.forEach {
                    contract.definitions.functions.add(it)
                }
        }
    }

    /**
     * In order to provide a default implementation for the contract operations, the expressions of the 'handle(string input)'
     * function generated by meivsm will be moved to the operations whose 'input' value matches the operation
     * name. E. g. Contract generated by meivsm contains one if expression with 'input' == 'op1()',
     * 'op1()' also exists in the contract modeled in LEMMA => Expressions of the meivsm contract will moved to the
     * LEMMA contract.
     *
     * Entry activities will be moved into the incoming transactions/operations (entry activities will be moved into
     * a separate function if more than two incoming transition exists).
     * Exit activities will be moved into the outgoing transactions/operations. => currently not implemented
     * Do activities will stay in the handle function => currently not implemented
     *
     * Algo:
     * - take only [ExpressionIf] instances whose condition contains a comparison against the variable 'input'
     * - extract the name of the function of the condition. Structure 'isEqual(input, "opName()")' (parentheses are optional)
     * - find opName in the [contract]
     * - if it exists add all activities to the operation
     * - remove the activities from the condition and replace it with the function call
     */
    private fun optimizeContract(contract: SmartContract, metaInfo: MetaInformation) {
        val handleIfFun = contract.definitions.functions.find {
            it.name == "handle"
        } ?: return

        val handleIfFunExpressionIf = handleIfFun.expressions[0]

        if (handleIfFunExpressionIf is ExpressionIf) {
            handleIfFunExpressionIf.conditions
                .forEach {
                    val condition = it.first
                    if (condition.contains("isEqual(input")) {
                        val regex = ".*isEqual\\(input, \"([\\w]*)(\\(.*\\))?\"\\).*".toRegex()
                        // our target is in the second match group (index 1)
                        val opName = regex.matchEntire(condition)?.groups?.get(1)?.value
                        if (opName != null) { // opName extracted. Lookup in contract
                            contract.definitions.functions.find { f -> f.name == opName }
                                ?.expressions?.addAll(it.second) // TODO exit activities are added too, but is not allowed!

                            it.second.clear()

                            // add call to operation. 3rd match group (index 2) contains params (already enclosed in parentheses)
                            val opParams = regex.matchEntire(condition)?.groups?.get(2)?.value
                            it.second.add(ExpressionStringImpl("$opName$opParams"))
                        }
                    }
                }
        }
    }
}
