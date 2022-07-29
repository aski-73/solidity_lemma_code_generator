package de.fhdo.lemma.model_processing.code_generation.solidity.serializers

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
 * 1) targetPath/ContractName.sol (Interface) => GenInterface (auto generated)
 *    - Contains all methods of provided [SmartContract] that are not hidden
 * 2) targetPath/ContractNameBaseImpl.sol => GenImpl (auto generated)
 *    - represents the [SmartContract]
 *
 * Generation is changed a bit if following files exist:
 * 1) targetPath/ContractNameBase.sol => UserDefinedInterface (user defined)
 *    - User defined interface
 * 2) targetPath/ContractNameImpl.sol => UserDefinedImpl (user defined)
 *    - User defined implementation that implements methods of user defined interface
 *    - Will be generated if the UserInterface exists and this file does not exist yet
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
            val genImpl = generateGenImpl(it, genInterface, targetFolderPath)
            val baseImplSerialized = extractor.generateSmartContractModel(genImpl)
            val genImplFile = File(targetFolderPath + "/" + genImpl.name)
            genImplFile.createNewFile()
            genImplFile.bufferedWriter().use { out -> out.write(baseImplSerialized) }
            print(baseImplSerialized)

            if (this.userDefinedInterface != null && this.userDefinedImpl == null) {
                this.userDefinedImpl = generateUserDefinedImpl(
                    this.userDefinedInterface!!,
                    genInterface.definitions.interfaces[0].name,
                    genImpl
                )
                val implFile = File(targetFolderPath + "/" + userDefinedImpl!!.name)
                val implFileSerialized = extractor.generateSmartContractModel(userDefinedImpl)
                implFile.createNewFile()
                implFile.bufferedWriter().use { out -> out.write(implFileSerialized) }
            }
        }

        return HashMap()
    }

    /**
     * Generates the Gen-Interface in the intermediate solidity representation by a provided [SmartContract]
     *
     * @param node [SmartContract] Intermediate Smart Contract instance
     *
     * @return [SmartContractModel] instance representing the file containing the base interface
     */
    private fun generateInterface(node: SmartContract, targetFolderPath: String): SmartContractModel {
        val model = SmartContractModelImpl(license, pragma)
        model.name = node.name + ".sol"
        val genInterface = InterfaceImpl(node.name)

        // Copy all methods that are not private or internal. Visibility is always public in order to build the contract interface
        // Not external, so that the implementing contract can call the methods
        node.definitions.functions
            .filter { it.visibility != Visibility.INTERNAL && it.visibility != Visibility.PRIVATE }
            .forEach {
                val f = FunctionImpl(it)
                // All declared functions must be external in the interface, even if they are public in the contract.
                // Solidity Docs 0.8.15
                f.visibility = Visibility.EXTERNAL
                f.statements.clear()
                genInterface.definitions.functions.add(f)
            }

        // Enumerations, structures will be moved to the interface because their types might be function arguments.
        // errors, events, stay at the base impl class because their types are not useful in function arguments.
        node.definitions.structures.forEach {
            genInterface.definitions.structures.add(StructureImpl(it))
        }
        node.definitions.structures.clear()

        // Adapt generated interface with user defined interface if it exists.
        val userDefinedInterface = findUserDefinedInterface(node, targetFolderPath)

        if (userDefinedInterface != null) {
            genInterface.extends.add(userDefinedInterface.second.definitions.interfaces[0])

            model.imports.add(
                ImportedConceptImpl(
                    "./" + userDefinedInterface.second.name,
                    userDefinedInterface.second.definitions
                )
            )
            this.userDefinedInterface = userDefinedInterface.second
        }

        model.definitions.interfaces.add(genInterface)

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
                MainContext.State.solidityParser.parse(userDefinedInterfaceFile[0].absolutePath)
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
                MainContext.State.solidityParser.parse(userDefinedImplFile[0].absolutePath)
            )
        }

        return null
    }

    /**
     * Creates a [SmartContract] instance that represents the base implementation (GenImpl) of the EGG-Gen-Interface
     *
     * @param node SmartContract instance delivered by the
     * [de.fhdo.lemma.model_processing.code_generation.solidity.handlers.CodeGenerationHandlerI]s
     * @param genInterface Representation of the Solidity file containing the EGG-Gen-Interface that should be
     * implemented
     *
     * @return [SmartContractModel] instance representing a solidity file containing the generated base implementation
     */
    private fun generateGenImpl(
        node: SmartContract,
        genInterface: SmartContractModel,
        targetFolderPath: String
    ): SmartContractModel {
        val model = SmartContractModelImpl(license, pragma)
        model.name = node.name + "BaseImpl.sol"

        val genImpl = SmartContractImpl(node)
        genImpl.name += "BaseImpl"

        // Import the baseInterface
        model.imports.add(
            ImportedConceptImpl(
//                targetFolderPath + baseInterface.definitions.interfaces[0].name + ".sol",
                "./" + genInterface.name,
                genInterface.definitions
            )
        )

        // Make it implement the base interface so that it becomes the base implementation
        genImpl.implements.add(genInterface.definitions.interfaces[0])

        val userDefinedImpl = findUserDefinedImplementation(node, targetFolderPath)

        // EGG requires to set the generated base implementation to abstract so that the user defined contract has to
        // extend it
        if (userDefinedImpl != null) {
            genImpl.isAbstract = true
            // Make every function that is not private virtual in order to make it overrideable by the user defined
            // implementation.
            genImpl.definitions.functions.forEach {
                if (it.visibility != Visibility.PRIVATE) {
                    it.isVirtual = true
                }
            }
            this.userDefinedImpl = userDefinedImpl.second
        }

        // Add empty implementations to the functions and modifiers who don't have an implementation yet
        genImpl.definitions.functions
            .filter { it.statements.size == 0 }
            .forEach {
                it.statements.add(StatementExpressionImpl(ExpressionImpl("revert(\"IMPLEMENTATION REQUIRED\")")))
            }
        genImpl.definitions.modifiers
            .filter { it.statements.size == 0 }
            .forEach {
                it.statements.add(StatementExpressionImpl(ExpressionImpl("revert(\"IMPLEMENTATION REQUIRED\")")))
            }

        // Set the 'override' key word for GenInterface-functions that are implemented by the GenImpl.
        // Those are all that are not private nor internal (same logic used when creating the Interface,
        // see generateInterface())
        genImpl.definitions.functions
            .filter { it.visibility != Visibility.INTERNAL && it.visibility != Visibility.PRIVATE }
            .forEach { it.doesOverride = true }

        model.definitions.contracts.add(genImpl)

        return model
    }

    /**
     * If the user defined a base interface, but has not created a corresponding implementing class,
     * the generator will define one.
     */
    private fun generateUserDefinedImpl(
        userDefinedInterface: SmartContractModel,
        genInterfaceName: String,
        baseImpl: SmartContractModel
    ): SmartContractModel {
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
            val returnExpression = "return" + (if (f.returns.size > 0) " ${defaultReturnValue(f.returns.map { it -> it.type.name })}" else "")
            f.statements.add(StatementExpressionImpl(ExpressionImpl(returnExpression)))
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
