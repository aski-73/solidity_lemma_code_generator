package de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.handlers

import de.fhdo.lemma.data.intermediate.IntermediateDataModel
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.CodeGenerationHandler
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.VisitingCodeGenerationHandlerI
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext.State as MainState
import net.aveyon.intermediate_solidity.SmartContractModel

@CodeGenerationHandler
class IntermediateDataModelHandler: VisitingCodeGenerationHandlerI<IntermediateDataModel, SmartContractModel, Any> {
    override fun handlesEObjectsOfInstance(): Class<IntermediateDataModel> = IntermediateDataModel::class.java

    override fun generatesNodesOfInstance(): Class<SmartContractModel> = SmartContractModel::class.java

    override fun execute(eObject: IntermediateDataModel, context: Any?): Pair<SmartContractModel, String?>? {
        val scm = GenerationUtil.mapToSmartContractModel(eObject, MainState.license, MainState.pragma)
        MainState.setCurrentGeneratedSmartContractModel(scm)

        return Pair(scm, null)
    }
}
