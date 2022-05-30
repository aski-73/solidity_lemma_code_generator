plugins {
    kotlin("jvm") version "1.6.10"
}

group = "de.fhdo.lemma.model_processing.code_generation.solidity"

repositories {
    mavenCentral()
    maven {
        // Repository of LEMMA artifacts
        url = uri("https://repository.seelab.fh-dortmund.de/repository/maven-public/")
    }
}


buildscript {
    extra.set("lemmaEclipsePluginsVersion", version)
    extra.set("log4jVersion", "2.16.0")
    extra.set("loggingVersion", "1.7.9")
    extra.set("modelProcessingVersion", version)
    extra.set("koinVersion", "2.0.1")
    extra.set("picocliVersion", "3.9.3")
    extra.set("jansiVersion", "1.17.1")
    extra.set("emfVersion", "2.24.0")
    extra.set("apacheCommonVersion", "3.12.0")
}

dependencies {
    val lemmaEclipsePluginsVersion: String by rootProject.extra
    val log4jVersion: String by rootProject.extra
    val modelProcessingVersion: String by rootProject.extra
    val koinVersion: String by rootProject.extra
    val picocliVersion: String by rootProject.extra
    val jansiVersion: String by rootProject.extra
    val loggingVersion: String by rootProject.extra
    val emfVersion: String by rootProject.extra
    val apacheCommonVersion: String by rootProject.extra

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // LEMMA DSLs
    implementation("de.fhdo.lemma.data.datadsl:de.fhdo.lemma.data.datadsl:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.data.datadsl:de.fhdo.lemma.data.datadsl.metamodel:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.technology.mappingdsl:de.fhdo.lemma.technology.mappingdsl:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.technology.mappingdsl:de.fhdo.lemma.technology.mappingdsl.metamodel:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.servicedsl:de.fhdo.lemma.servicedsl:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.intermediate:de.fhdo.lemma.data.intermediate.metamodel:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.intermediate:de.fhdo.lemma.service.intermediate.metamodel:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.live_validation:de.fhdo.lemma.live_validation.util:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.live_validation:de.fhdo.lemma.live_validation.model:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.live_validation:de.fhdo.lemma.live_validation.protocol:$lemmaEclipsePluginsVersion")
    implementation("de.fhdo.lemma.live_validation:de.fhdo.lemma.live_validation.client:$lemmaEclipsePluginsVersion")

    // LEMMA Model Processing Framework
//    implementation("de.fhdo.lemma.model_processing:de.fhdo.lemma.model_processing:$modelProcessingVersion:all-dependencies")
    implementation("de.fhdo.lemma.model_processing:de.fhdo.lemma.model_processing:$modelProcessingVersion")
    implementation("de.fhdo.lemma.model_processing.utils:de.fhdo.lemma.model_processing.utils:$modelProcessingVersion")

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("io.github.microutils:kotlin-logging:$loggingVersion")

    // Dependency Injection
    implementation("io.insert-koin:koin-core:$koinVersion")

    // For Command Line
    implementation("info.picocli:picocli:$picocliVersion")
    implementation("org.fusesource.jansi:jansi:$jansiVersion")

    // Intermediate Solidity
    implementation(files("./lib/intermediate_solidity-1.0-SNAPSHOT.jar"))

    // Intermediate Solidity Extractor
    implementation(files("./lib/intermediate_solidity_extractor-1.0-SNAPSHOT.jar"))

    // Solidity Parser
    implementation(files("./lib/solidity_parser-1.0-SNAPSHOT.jar"))

    // Meivsm (State UML to Solidity)
    implementation(files("./lib/meivsm-compiler-1.0.jar"))

    // ANTLR. Needed because without meivsm won't run (ClassNotFound Exception on TokenSource class)
    // https://mvnrepository.com/artifact/org.antlr/antlr4
    implementation("org.antlr:antlr4:4.10.1")



    // Apache Common
    implementation("org.apache.commons:commons-lang3:$apacheCommonVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    // Support @JvmDefault annotation
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=compatibility")
}


/**
 * standalone task to create a standalone runnable JAR of the Solidity Generator
 */
val standalone = task("standalone", type = Jar::class) {
    archiveClassifier.set("standalone")

    // Build fat JAR
    from(configurations.compileClasspath.get().filter{ it.exists() }.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)

    manifest {
        attributes("Main-Class" to "de.fhdo.lemma.model_processing.code_generation.solidity.SolidityGenerator")
        // Prevent "WARNING: sun.reflect.Reflection.getCallerClass is not supported" from log4j
        attributes("Multi-Release" to "true")

        // Prevent security exception from JAR verifier
        exclude("META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.SF")
    }
}
