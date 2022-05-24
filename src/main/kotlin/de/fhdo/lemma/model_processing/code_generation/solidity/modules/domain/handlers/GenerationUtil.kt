package de.fhdo.lemma.model_processing.code_generation.solidity.modules.domain.handlers

import de.fhdo.lemma.data.intermediate.*
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.SolidityTechnology
import de.fhdo.lemma.model_processing.code_generation.solidity.simpleName
import net.aveyon.intermediate_solidity.*
import net.aveyon.intermediate_solidity.impl.*

/**
 * Helper class for mapping classes from the intermediate data model to intermediate solidity
 */
class GenerationUtil {
    companion object {

        fun mapToSmartContractModel(eObject: IntermediateDataModel, license: String, pragma: String): SmartContractModel {
            val scm = SmartContractModelImpl("GPL-3.0", "^0.8.4")

            /**
             * Add definitions (contracts, events, errors, enums)
             */

            // contracts: see IntermediateDataStructureHandler

            return scm
        }

        /**
         * Maps [IntermediateDataStructure] to [SmartContract]. Adds only to fields and operations.
         * Other concepts like errors, events, enums are handled by [IntermediateDataStructureHandler]
         */
        fun mapToSmartContract(eObject: IntermediateDataStructure): SmartContract {
            val sc = SmartContractImpl(eObject.name)
            // add fields
            eObject.dataFields.forEach {
                sc.fields.add(mapToField(it))
            }

            // add operations
            eObject.operations.forEach {
                sc.definitions.functions.add(mapToOperation(it))
            }

            return sc
        }

        /**
         * Maps [IntermediateDataStructure] to [Structure]
         * Solidity structures don't have operations. Therefore ignore them in the source eObject.
         */
        fun mapToStructure(eObject: IntermediateDataStructure): Structure {
            val struct = StructureImpl(eObject.name)

            // add fields
            eObject.dataFields.forEach {
                struct.fields.add(mapToLocalField(it))
            }

            return struct
        }

        /**
         * Maps [IntermediateDataStructure] to [Event]
         */
        fun mapToEvent(eObject: IntermediateDataStructure): Event {
            val event = EventImpl(eObject.name)
            eObject.dataFields.forEach {
                event.eventParams.add(mapToLocalField(it))
            }
            return event
        }

        /**
         * Maps [IntermediateDataStructure] to [Error]
         */
        fun mapToError(eObject: IntermediateDataStructure): Error {
            val event = ErrorImpl(eObject.name)
            eObject.dataFields.forEach {
                event.errorParams.add(mapToLocalField(it))
            }
            return event
        }

        /**
         * Maps [IntermediateDataField] to [Field]
         */
        fun mapToField(eObject: IntermediateDataField): Field {
            val parentType = mapToLocalField(eObject)

            // Share logic to compute common attributes
            val field = FieldImpl(parentType.name)
            field.type = parentType.type
            field.array = parentType.array
            field.visibility = mapToFieldVisibility(eObject)
            field.payable = parentType.payable

            return field
        }

        /**
         * Maps [IntermediateDataField] to [StructureField]
         */
        fun mapToLocalField(eObject: IntermediateDataField): LocalField {
            val localField = LocalFieldImpl(eObject.name)
            localField.type = mapTypes(eObject.type)
            localField.array = eObject.listType != null
            localField.payable = eObject.aspects.any{ it.qualifiedName.equals(SolidityTechnology.ASPECT_PAYABLE.value)}

            return localField
        }

        /**
         * Maps [IntermediateDataOperation] to [Operation]
         */
        fun mapToOperation(eObject: IntermediateDataOperation): net.aveyon.intermediate_solidity.Function {
            val f = FunctionImpl(eObject.name)
            f.visibility = mapToDataOperationVisibility(eObject)
            if (eObject.returnType != null) {
                f.returns.add(eObject.returnType.simpleName)
            }
            eObject.parameters.forEach {
                f.parameters.add(mapToParameter(it))
            }
            return f
        }

        /**
         * Maps [IntermediateDataOperationParameter] to [OperationParameter]
         */
        fun mapToParameter(eObject: IntermediateDataOperationParameter): FunctionParameter {
            val param = FunctionParameterImpl(eObject.name)
            param.type = mapTypes(eObject.type)
            param.dataLocation = DataLocation.MEMORY
            return param
        }

        fun mapToFieldVisibility(eObject: IntermediateDataField): Visibility {
            return if (eObject.isHidden)
                Visibility.PRIVATE
            else
                Visibility.INTERNAL
        }

        fun mapToDataOperationVisibility(eObject: IntermediateDataOperation): Visibility {
            return if (eObject.isHidden) {
                Visibility.PRIVATE
            } else {
                Visibility.PUBLIC
            }
        }

        /**
         * Maps [IntermediateEnumeration] to [Enumeration]
         */
        fun mapToEnumeration(eObject: IntermediateEnumeration): Enumeration {
            val enumeration = EnumerationImpl(eObject.name)
            eObject.fields.forEach {
                enumeration.values.add(it.name)
            }

            return enumeration
        }

        fun mapTypes(eObject: IntermediateType): String {
            return if (eObject is IntermediateComplexType) {
                eObject.name
            } else {
                when (eObject.name) {
                    "date" -> "uint"
                    else -> eObject.name
                }
            }
        }
    }
}