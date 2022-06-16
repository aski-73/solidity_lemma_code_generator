package de.fhdo.lemma.model_processing.code_generation.solidity.serializers

import de.fhdo.lemma.model_processing.code_generation.solidity.SolidityParser
import de.fhdo.lemma.model_processing.code_generation.solidity.handlers.VisitingCodeGenerationHandlerI
import de.fhdo.lemma.model_processing.code_generation.solidity.modules.MainContext
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
    val pragma: String = ">= 0.8.0 <0.9.0"
    val extractor = IntermediateSolidityExtractor()

    var userDefinedImpl: SmartContractModel? = null
    var userDefinedInterface: SmartContractModel? = null

    internal fun serialize(node: SmartContractModel, targetFolderPath: String, targetFilePath: String)
            : Map<String, String> {
        val dir = File(targetFolderPath)

        if (!dir.isDirectory) {
            println("targetFolderPath is not a directory")
            return HashMap()
        }

        node.definitions.contracts.forEach {
            // Generate base interface based on the provided node
            val genInterface = generateInterface(it, targetFolderPath)
            // code generation
            val interfaceSerialized = extractor.generateSmartContractModel(genInterface)
            val interfaceFile = File(targetFolderPath + "/" + genInterface.name)
            interfaceFile.createNewFile()
            interfaceFile.bufferedWriter().use { out -> out.write(interfaceSerialized) }
            print(interfaceSerialized)

            // Generate a base implementation for the generated interface
            val baseImpl = generateBaseImpl(it, genInterface, targetFolderPath)
            val baseImplSerialized = extractor.generateSmartContractModel(baseImpl)
            val baseImplFile = File(targetFolderPath + "/" + baseImpl.name)
            baseImplFile.createNewFile()
            baseImplFile.bufferedWriter().use { out -> out.write(baseImplSerialized) }
            print(baseImplSerialized)

            if (this.userDefinedInterface != null && this.userDefinedImpl == null) {
                val impl = generateImpl(this.userDefinedInterface!!, genInterface.definitions.interfaces[0].name, baseImpl)
                val implFile = File(targetFolderPath + "/" + impl.name)
                val implFileSerialized = extractor.generateSmartContractModel(impl)
                implFile.createNewFile()
                implFile.bufferedWriter().use { out -> out.write(implFileSerialized) }
            }
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
    private fun generateInterface(node: SmartContract, targetFolderPath: String): SmartContractModel {
        val model = SmartContractModelImpl(license, pragma)
        model.name = node.name + ".sol"
        val baseInterface = InterfaceImpl(node.name)

        // Copy all methods that are not private or internal. Visibility is always public in order to build the contract interface
        // Not external, so that the implementing contract can call the methods
        node.definitions.functions
            .filter { it.visibility != Visibility.INTERNAL && it.visibility != Visibility.PRIVATE }
            .forEach {
                val f = FunctionImpl(it)
                // All declared functions must be external in the interface, even if they are public in the contract.
                // Solidity Docs 0.8.14
                f.visibility = Visibility.EXTERNAL
                f.expressions.clear()
                baseInterface.definitions.functions.add(f)
            }

        // Enumerations, structures will be moved to the interface because their types might be function arguments.
        // errors, events, stay at the base impl class because their types are not useful in function arguments.
        node.definitions.structures.forEach {
            baseInterface.definitions.structures.add(StructureImpl(it))
        }
        node.definitions.structures.clear()

        // Adapt generated interface with user defined interface if it exists.
        val userDefinedInterface = findUserDefinedInterface(node, targetFolderPath)

        if (userDefinedInterface != null) {
            baseInterface.extends.add(userDefinedInterface.second.definitions.interfaces[0])

            model.imports.add(
                ImportedConceptImpl(
                    "./" + userDefinedInterface.second.name,
                    userDefinedInterface.second.definitions
                )
            )
            this.userDefinedInterface = userDefinedInterface.second
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
        val userDefinedInterfaceFile = dir.listFiles()?.filter { it.name.equals(node.name + "Base.sol") }

        if (userDefinedInterfaceFile != null && userDefinedInterfaceFile.isNotEmpty()) {
            return Pair(
                userDefinedInterfaceFile[0].absolutePath,
                SolidityParser.parse(userDefinedInterfaceFile[0].absolutePath)
            )
        }

        return null
    }

    /**
     * Checks if a user defined implementation exists
     *
     * @return Pair of interface file path and parsed [SmartContractModel] instance that represents the user defined
     * contract implementation
     */
    private fun findUserDefinedImplementation(
        node: SmartContract,
        targetFolderPath: String
    ): Pair<String, SmartContractModel>? {
        val dir = File(targetFolderPath)
        assert(dir.isDirectory)

        // Look for a file that applies to the naming convention (currently the contract name)
        val userDefinedImplFile = dir.listFiles()?.filter { it.name.equals(node.name + "Impl.sol") }

        if (userDefinedImplFile != null && userDefinedImplFile.isNotEmpty()) {
            return Pair(
                userDefinedImplFile[0].absolutePath,
                SolidityParser.parse(userDefinedImplFile[0].absolutePath)
            )
        }

        return null
    }

    /**
     * Creates a [SmartContract] instance that represents the base implementation of the EGG-Base-Interface
     *
     * @param node SmartContract instance delivered by the [VisitingCodeGenerationHandlerI]s
     * @param baseInterface Representation of the Solidity file containing the EGG-Base-Interface that should be implemented
     *
     * @return [SmartContractModel] instance representing a solidity file containing the generated base implementation
     */
    private fun generateBaseImpl(
        node: SmartContract,
        baseInterface: SmartContractModel,
        targetFolderPath: String
    ): SmartContractModel {
        val model = SmartContractModelImpl(license, pragma)
        model.name = node.name + "BaseImpl.sol"

        val baseImpl = SmartContractImpl(node)
        baseImpl.name += "BaseImpl"

        // Import the baseInterface
        model.imports.add(
            ImportedConceptImpl(
//                targetFolderPath + baseInterface.definitions.interfaces[0].name + ".sol",
                "./" + baseInterface.name,
                baseInterface.definitions
            )
        )

        // Make it implement the base interface so that it becomes the base implementation
        baseImpl.implements.add(baseInterface.definitions.interfaces[0])

        val userDefinedImpl = findUserDefinedImplementation(node, targetFolderPath)

        // EGG requires to set the generated base implementation to abstract so that the user defined contract has to
        // extend it
        if (userDefinedImpl != null) {
            baseImpl.isAbstract = true
            // Make every function that is not private virtual in order to make it overrideable by the user defined
            // implementation.
            baseImpl.definitions.functions.forEach {
                if (it.visibility != Visibility.PRIVATE) {
                    it.isVirtual = true
                }
            }
            this.userDefinedImpl = userDefinedImpl.second
        }

        // Add empty implementations to the functions and modifiers
        baseImpl.definitions.functions
            .filter { it.expressions.size == 0 }
            .forEach {
                it.expressions.add(ExpressionStringImpl("revert(\"IMPLEMENTATION REQUIRED\")"))
            }
        baseImpl.definitions.modifiers
            .filter { it.expressions.size == 0 }
            .forEach {
                it.expressions.add(ExpressionStringImpl("revert(\"IMPLEMENTATION REQUIRED\")"))
            }
        model.definitions.contracts.add(baseImpl)

        return model
    }

    /**
     * If the user defined a base interface, but has not created a corresponding implementing class,
     * the generator will define one.
     */
    private fun generateImpl(userDefinedInterface: SmartContractModel,
                             genInterfaceName: String,
                             baseImpl: SmartContractModel): SmartContractModel {
        val userDefinedImpl = SmartContractModelImpl(MainContext.State.license, MainContext.State.pragma)
        userDefinedImpl.name = genInterfaceName + "Impl.sol"

        // Import the baseImpl
        userDefinedImpl.imports.add(
            ImportedConceptImpl(
                "./" + baseImpl.name,
                baseImpl.definitions
            )
        )
        // Create contract that extends the base Impl and contains default method implementation for the
        // user defined interface.
        val contract = SmartContractImpl(genInterfaceName + "Impl")
        userDefinedInterface.definitions.interfaces[0].definitions.functions.forEach {
            val f = FunctionImpl(it)
            f.isAbstract = false
            f.doesOverride = true
            val returnExpression = "return" + (if (f.returns.size > 0) " ${defaultReturnValue(f.returns)}" else "")
            f.expressions.add(ExpressionStringImpl(returnExpression))
            contract.definitions.functions.add(f)
        }
        contract.extends.add(baseImpl.definitions.contracts[0])

        userDefinedImpl.definitions.contracts.add(contract)

        return userDefinedImpl
    }

    /**
     * Depending on the return value type, a default return value will be provided.
     */
    private fun defaultReturnValue(returnTypes: List<String>): String {
        var returnValue = ""

        returnTypes.forEach {
            val r = when (it) {
                "uint" -> "0"
                "int" -> "-1"
                "bool" -> "false"
                else -> {
                    return ""
                }
            }
            returnValue += "$r,"
        }

        if (returnValue.endsWith(",")) {
            returnValue = returnValue.substring(0, returnValue.length - 1)
        }

        if (returnTypes.size > 1) {
            returnValue = "($returnValue)"
        }

        return returnValue
    }
}
