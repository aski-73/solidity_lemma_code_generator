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
        val isContract = eObject.aspects.any { it.qualifiedName.equals(SolidityTechnology.ASPECT_STATE_BEHAVIOR.value) }
        val isError = eObject.featureNames.any { it.equals(ComplexTypeFeature.DOMAIN_EVENT.name) }
                && eObject.aspects.any { it.qualifiedName.equals(SolidityTechnology.ASPECT_ERROR.value) }
        // Need to include isError in order not to set both to true (both contain the event feature)
        val isEvent = !isError && eObject.featureNames.any { it.equals(ComplexTypeFeature.DOMAIN_EVENT.name) }

        val currentGeneratedSmartContract: SmartContract by DomainState

        return if (isContract) {
            val sc = mapToSmartContract(eObject)
            DomainState.setCurrentGeneratedSmartContract(sc)
            val currentGeneratedSmartContractModel: SmartContractModel by MainState
            currentGeneratedSmartContractModel.definitions.contracts.add(sc)
            // TODO call meivsm and generate handle() method
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
}
