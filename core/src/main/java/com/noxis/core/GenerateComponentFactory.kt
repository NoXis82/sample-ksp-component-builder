package com.noxis.core

/**
 * помечать компоненты, для которых нужно сгенерировать фабрику.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class GenerateComponentFactory()
