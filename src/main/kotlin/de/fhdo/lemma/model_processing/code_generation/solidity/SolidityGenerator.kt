package de.fhdo.lemma.model_processing.code_generation.solidity

import de.fhdo.lemma.model_processing.AbstractModelProcessor

/**
 * Entrypoint of the Solidity Generator.
 *
 * This generator takes a Intermediate Service Model as an input (.xmi file) which is generated from
 * a LEMMA Mapping Model by a LEMMA Eclipse Plugin (right click on model -> Generate Intermediate Service Models).
 * The Mapping Model must contain a type mapping for a Domain Model Context with the aspect "StateBehavior".
 *
 */
class SolidityGenerator : AbstractModelProcessor("de.fhdo.lemma.model_processing.code_generation.solidity") {}

/**
 * Program entrypoint
 */
fun main(args: Array<String>) {
    SolidityGenerator().run(args)
}
