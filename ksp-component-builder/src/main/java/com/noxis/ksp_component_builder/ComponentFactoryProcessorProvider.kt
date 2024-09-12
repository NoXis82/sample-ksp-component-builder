package com.noxis.ksp_component_builder

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * провайдер процессора
 */
class ComponentFactoryProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ComponentFactoryProcessor(
            logger = environment.logger,
            fileGenerator = ComponentFactoryFileGenerator(environment.codeGenerator)
        )
    }
}