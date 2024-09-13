package com.noxis.contact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.noxis.core.DependenciesProvider
import com.noxis.core.provide
import okhttp3.OkHttpClient
import javax.inject.Inject

class ContactsActivity : ComponentActivity() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        DaggerContactsComponent.builder()
//            .requiredParameterModule(RequiredParameterModule(this))
//            .allDefaultParametersModule(AllDefaultParametersModule()) // we can omit it
            .unknownDependencies(object : UnknownDependencies {})
            .networkDependencies(DependenciesProvider.instance.provide()) // we can omit it
            .internalProvidableDependencies(DependenciesProvider.instance.provide()) // we can omit it
            .build()
            .inject(this)

        // VS

        ContactsComponentFactory.createComponent(
            requiredParameterModule = RequiredParameterModule(this),
            unknownDependencies = object : UnknownDependencies {},
            allDefaultParametersModule = AllDefaultParametersModule(),
            networkDependencies = DependenciesProvider.instance.provide(),
            internalProvidableDependencies = DependenciesProvider.instance.provide()
        ).inject(this)

    }
}