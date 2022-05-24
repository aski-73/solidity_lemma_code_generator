package de.fhdo.lemma.model_processing.code_generation.solidity

import net.aveyon.App
import net.aveyon.intermediate_solidity.Interface
import net.aveyon.intermediate_solidity.SmartContract
import net.aveyon.intermediate_solidity.SmartContractModel

/**
 * Proxy between the actual Solidity Parser and the Solidity code generator
 */
class SolidityParser {

    companion object {
        /**
         * Returns the first interface found in the provided Solidity file
         */
        fun parseInterface(file: String): Interface {
            return App.parse(file).definitions.interfaces[0];
        }

        /**
         * Returns the first Smart Contract found in the provided Solidity file
         */
        fun parseContract(file: String): SmartContract {
            return App.parse(file).definitions.contracts[0];
        }

        /**
         * Returns the whole provided Solidity file as an [SmartContractModel]
         */
        fun parse(file: String): SmartContractModel {
            return App.parse(file)
        }
    }
}
