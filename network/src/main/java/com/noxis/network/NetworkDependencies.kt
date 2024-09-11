package com.noxis.network

import com.noxis.core.ProvidableDependency
import okhttp3.OkHttpClient


/**
 * Интерфейс будет уметь предоставлять ссылку на OkHttpClient и
 * будет промаркирован интерфейсом ProvidableDependency из модуля core,
 * обозначая таким образом свою способность быть полученным из DependenciesProvider
 */
interface NetworkDependencies : ProvidableDependency {

    fun getOkHttpClient(): OkHttpClient
}