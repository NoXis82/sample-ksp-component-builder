package com.noxis.contact

import com.noxis.core.GenerateComponentFactory
import com.noxis.network.NetworkDependencies
import dagger.Component

/**
 * Компонент будет уметь делать inject() в ContactsActivity
 * и будет иметь разнообразные зависимости
 * InterfaceModule - интерфейс Dagger модулей
 * AllDefaultParametersModule - класс с конструктором по умолчанию
 * RequiredParameterModule - класс с обязательными параметрами
 */

@GenerateComponentFactory
@Component(
    modules = [
        InterfaceModule::class,
        AllDefaultParametersModule::class,
        RequiredParameterModule::class
    ],
    dependencies = [
        NetworkDependencies::class,
        InternalProvidableDependencies::class,
        UnknownDependencies::class
    ]
)
internal interface ContactsComponent {
    fun inject(activity: ContactsActivity)
}