package de.fhdo.lemma.model_processing.code_generation.solidity.serializers

import de.fhdo.lemma.model_processing.code_generation.solidity.SolidityParser
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.VisitingCodeGenerationHandlerI
import net.aveyon.intermediate_solidity.SmartContract
import net.aveyon.intermediate_solidity.SmartContractModel
import net.aveyon.intermediate_solidity.Visibility
import net.aveyon.intermediate_solidity.impl.*
import net.aveyon.intermediate_solidity_extractor.IntermediateSolidityExtractor
import org.eclipse.emf.ecore.EObject
import org.koin.core.KoinComponent
import java.io.File

/**
 * Generate files (overwrites if already exist):
 * 1) targetPath/ContractNameBase.sol (Interface)
 *    - Contains all methods of provided [SmartContract] that are not hidden
 * 2) targetPath/ContractNameBaseImpl.sol
 *    - represents the [SmartContract]
 *
 * Generation is changed a bit if following files exist:
 * 1) targetPath/ContractName.sol
 *    - User defined interface
 * 2) targetPath/ContractNameImpl.sol
 *    - User defined implementation that implements methods of user defined interface
 */
@CodeGenerationSerializer("extended-generation-gap", default = true)
class ExtendedGenerationGapSerializer : CodeGenerationSerializerI, KoinComponent {
    private val generatorDelegate = ExtendedGenerationGapSerializerBase()

    /**
     * @return Map containing 2 elements. 1st: path -> BaseInterface 2nd: path -> BaseImpl
     */
    override fun serialize(
        node: SmartContractModel,
        targetFolderPath: String,
        targetFilePath: String,
        intermediateEObject: EObject?,
        originalModelFilePath: String?
    ): Map<String, String> {
        return generatorDelegate.serialize(node, targetFolderPath, targetFilePath)
    }
}

private class ExtendedGenerationGapSerializerBase {
    val license: String = "GPL 3.0"
    val pragma: String = "<0.9.0"
    val extractor = IntermediateSolidityExtractor()

    internal fun serialize(node: SmartContractModel, targetFolderPath: String, targetFilePath: String)
            : Map<String, String> {
        val dir = File(targetFolderPath)

        if (!dir.isDirectory) {
            println("targetFolderPath is not a directory")
            return HashMap()
        }

        node.definitions.contracts.forEach {
            // Generate base interface based on the provided node
            val baseInterface = generateBaseInterface(it, targetFolderPath)
            // code generation
            val baseInterfaceSerialized = extractor.generateSmartContractModel(baseInterface)
            val baseInterfaceFile = File(targetFolderPath + "/" + baseInterface.name)
            baseInterfaceFile.createNewFile()
            baseInterfaceFile.bufferedWriter().use { out -> out.write(baseInterfaceSerialized) }
            print(baseInterfaceSerialized)

            // Generate a base implementation for the generated interface
            val baseImpl = generateBaseImpl(it, baseInterface, targetFolderPath)
            val baseImplSerialized = extractor.generateSmartContractModel(baseImpl)
            val baseImplFile = File(targetFolderPath + "/" + baseImpl.name)
            baseImplFile.createNewFile()
            baseImplFile.bufferedWriter().use { out -> out.write(baseImplSerialized) }
            print(baseImplSerialized)
        }

        return HashMap()
    }

    /**
     * Generates the Base Interface in the intermediate solidity representation by a provided [SmartContract]
     *
     * @param node [SmartContract] Intermediate Smart Contract instance
     *
     * @return [SmartContractModel] instance representing the file containing the base interface
     */
    private fun generateBaseInterface(node: SmartContract, targetFolderPath: String): SmartContractModel {
        val model = SmartContractModelImpl(license, pragma)
        model.name = node.name + "Base.sol"
        val baseInterface = InterfaceImpl(node.name + "Base")

        // Copy all methods that are not private or internal. Visibility is always public in order to build the contract interface
        // Not external, so that the implementing contract can call the methods
        node.definitions.functions
            .filter { it.visibility != Visibility.INTERNAL && it.visibility != Visibility.PRIVATE }
            .forEach {
                val f = FunctionImpl(it);
                f.expressions.clear()
                baseInterface.definitions.functions.add(f)
            }

        // Enumerations, errors, events, structures stay at the base impl class

        // Adapt generated interface with user defined interface if it exists.
        val userDefinedInterface = findUserDefinedInterface(node, targetFolderPath)

        if (userDefinedInterface != null) {
            baseInterface.extends.add(userDefinedInterface.second.definitions.interfaces[0])

            model.imports.add(
                ImportedConceptImpl(
                    userDefinedInterface.first,
                    userDefinedInterface.second.definitions
                )
            )
        }

        model.definitions.interfaces.add(baseInterface)

        return model
    }

    /**
     * Checks if a user defined interface exists
     *
     * @return Pair of interface file path and parsed [SmartContractModel] instance that represents the interface file
     */
    private fun findUserDefinedInterface(
        node: SmartContract,
        targetFolderPath: String
    ): Pair<String, SmartContractModel>? {
        val dir = File(targetFolderPath)
        assert(dir.isDirectory)

        // Look for a file that applies to the naming convention (currently the contract name)
        val userDefinedInterfaceFile = dir.listFiles()?.filter { it.name.equals(node.name) }

        if (userDefinedInterfaceFile != null && userDefinedInterfaceFile.isNotEmpty()) {
            return Pair(
                userDefinedInterfaceFile[0].absolutePath,
                SolidityParser.parse(userDefinedInterfaceFile[0].absolutePath)
            )
        }

        return null
    }

    /**
     * Checks if a user defined interface exists
     *
     * @return Pair of interface file path and parsed [SmartContractModel] instance that represents the user defined
     * contract implementation
     */
    private fun findUserDefinedImplementation(
        node: SmartContract,
        targetFolderPath: String
    ): Pair<String, SmartContract>? {
        val dir = File(targetFolderPath)
        assert(dir.isDirectory)

        // Look for a file that applies to the naming convention (currently the contract name)
        val userDefinedBaseImplFile = dir.listFiles()?.filter { it.name.equals(node.name + "Impl") }

        if (userDefinedBaseImplFile != null && userDefinedBaseImplFile.isNotEmpty()) {
            return Pair(
                userDefinedBaseImplFile[0].absolutePath,
                SolidityParser.parseContract(userDefinedBaseImplFile[0].absolutePath)
            )
        }

        return null
    }

    /**
     * Creates a [SmartContract] instance that represents the base implementation of the EGP-Base-Interface
     *
     * @param node SmartContract instance delivered by the [VisitingCodeGenerationHandlerI]s
     * @param baseInterface Representation of the Solidity file containing the EGP-Base-Interface that should be implemented
     *
     * @return [SmartContractModel] instance representing a solidity file containing the generated base implementation
     */
    private fun generateBaseImpl(
        node: SmartContract,
        baseInterface: SmartContractModel,
        targetFolderPath: String
    ): SmartContractModel {
        val model = SmartContractModelImpl(license, pragma)
        model.name = node.name + ".sol"

        val baseImpl = SmartContractImpl(node)

        // Import the baseInterface
        model.imports.add(
            ImportedConceptImpl(
//                targetFolderPath + baseInterface.definitions.interfaces[0].name + ".sol",
                "./" + baseInterface.definitions.interfaces[0].name + ".sol",
                baseInterface.definitions
            )
        )

        // Make it implement the base interface so that it becomes the base implementation
        baseImpl.implements.add(baseInterface.definitions.interfaces[0])

        val userDefinedBaseImpl = findUserDefinedImplementation(node, targetFolderPath)

        // EGP requires to set the generated base implementation to abstract so that the user defined contract has to
        // extend it
        if (userDefinedBaseImpl != null) {
            baseImpl.isAbstract = true
        }

        // Add empty implementations to the functions
        baseImpl.definitions.functions
            .filter { it.expressions.size == 0 }
            .forEach {
                it.expressions.add(ExpressionStringImpl("// TODO: IMPLEMENT ME"))
            }

        model.definitions.contracts.add(baseImpl)

        return model
    }
}
