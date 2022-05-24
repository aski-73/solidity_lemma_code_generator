package de.fhdo.lemma.model_processing.code_generation.solidity.languages

import de.fhdo.lemma.ServiceDslStandaloneSetup
import de.fhdo.lemma.data.DataDslStandaloneSetup
import de.fhdo.lemma.data.DataPackage
import de.fhdo.lemma.model_processing.annotations.LanguageDescriptionProvider
import de.fhdo.lemma.model_processing.languages.LanguageDescriptionProviderI
import de.fhdo.lemma.model_processing.languages.XmiLanguageDescription
import de.fhdo.lemma.model_processing.languages.XtextLanguageDescription
import de.fhdo.lemma.service.ServicePackage
import de.fhdo.lemma.technology.TechnologyDslStandaloneSetup
import de.fhdo.lemma.technology.TechnologyPackage
import de.fhdo.lemma.technology.mapping.MappingPackage
import de.fhdo.lemma.technology.mappingdsl.MappingDslStandaloneSetup

import de.fhdo.lemma.data.intermediate.IntermediatePackage as IntermediateDataPackage
import de.fhdo.lemma.service.intermediate.IntermediatePackage as IntermediateServicePackage

/**
 * At runtime, a language
 * description provider informs the model processing framework about the modeling languages of the input models to
 * process. Among others, these information allow the framework to parse the input models and hide the related
 * complexity from the model processor implementer.
 */
@LanguageDescriptionProvider
class LemmaLanguageDescription : LanguageDescriptionProviderI {
    /**
     * The [de.fhdo.lemma.model_processing.languages.LanguageDescriptionProviderI] interface defines the
     * [de.fhdo.lemma.model_processing.languages.LanguageDescriptionProviderI.getLanguageDescription] method. At
     * runtime, the model processing framework invokes the method with a namespace (for XMI-based models) or file
     * extension (for models that do not contain directly extractable information about their namespace, e.g.,
     * Xtext-based models in their textual form) that identify an Eclipse-based modeling language. The flags
     * `forLanguageNamespace` and `forFileExtension` flags inform model processors about whether the
     * `languageNamespaceOrFileExtension` parameter contains a namespace or file extension. The model processing
     * framework then expects the [de.fhdo.lemma.model_processing.languages.LanguageDescriptionProviderI.getLanguageDescription]
     * method to return an instance of [de.fhdo.lemma.model_processing.languages.LanguageDescription], which comprises all
     * relevant information for the framework to, e.g., parse an input model.
     */
    override fun getLanguageDescription(
        forLanguageNamespace: Boolean, forFileExtension: Boolean,
        languageNamespaceOrFileExtension: String
    ) =
        when (languageNamespaceOrFileExtension) {
            DataPackage.eNS_URI, "data" -> DATA_DSL_LANGUAGE_DESCRIPTION
            MappingPackage.eNS_URI, "mapping" -> MAPPING_DSL_LANGUAGE_DESCRIPTION
            ServicePackage.eNS_URI, "services" -> SERVICE_DSL_LANGUAGE_DESCRIPTION
            TechnologyPackage.eNS_URI, "technology" -> TECHNOLOGY_DSL_LANGUAGE_DESCRIPTION
            IntermediateDataPackage.eNS_URI -> INTERMEDIATE_DATA_MODEL_LANGUAGE_DESCRIPTION
            IntermediateServicePackage.eNS_URI -> INTERMEDIATE_SERVICE_MODEL_LANGUAGE_DESCRIPTION
            else -> null
        }
}

/**
 * [XtextLanguageDescription] for the Data DSL.
 */
val DATA_DSL_LANGUAGE_DESCRIPTION =
    XtextLanguageDescription(DataPackage.eINSTANCE, DataDslStandaloneSetup())

/**
 * [XtextLanguageDescription] for the Mapping DSL.
 */
val MAPPING_DSL_LANGUAGE_DESCRIPTION =
    XtextLanguageDescription(MappingPackage.eINSTANCE, MappingDslStandaloneSetup())

/**
 * [XtextLanguageDescription] for the Service DSL.
 */
val SERVICE_DSL_LANGUAGE_DESCRIPTION = XtextLanguageDescription(
    ServicePackage.eINSTANCE,
    ServiceDslStandaloneSetup()
)

/**
 * [XtextLanguageDescription] for the Technology DSL.
 */
val TECHNOLOGY_DSL_LANGUAGE_DESCRIPTION = XtextLanguageDescription(
    ServicePackage.eINSTANCE,
    TechnologyDslStandaloneSetup()
)


/**
 * [XmiLanguageDescription] for intermediate data models.
 */
internal val INTERMEDIATE_DATA_MODEL_LANGUAGE_DESCRIPTION =
    XmiLanguageDescription(IntermediateDataPackage.eINSTANCE)

/**
 * [XmiLanguageDescription] for intermediate service models.
 */
internal val INTERMEDIATE_SERVICE_MODEL_LANGUAGE_DESCRIPTION =
    XmiLanguageDescription(IntermediateServicePackage.eINSTANCE)
