package com.noxis.core

import kotlin.reflect.KClass

/**
 * Он должен уметь хранить ссылку на свою реализацию,
 * чтобы быть доступным кому и откуда угодно
 * Содержит метод provide(), который может принимать ссылку на KClass,
 * типизированный наследником ProvidableDependency и
 * возвращать в ответ экземпляр класса, реализующего переданный тип.
 */
interface DependenciesProvider {

    fun <T : ProvidableDependency> provide(clazz: KClass<T>): T

    companion object {
        val instance: DependenciesProvider
            get() = TODO("Somehow implemented")
    }

}

inline fun <reified T : ProvidableDependency> DependenciesProvider.provide(): T {
    return provide(T::class)
}