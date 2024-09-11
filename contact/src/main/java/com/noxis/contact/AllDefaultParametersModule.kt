package com.noxis.contact

import dagger.Module
import java.util.UUID

@Module
class AllDefaultParametersModule(
    private val key: String = UUID.randomUUID().toString(),
    private val timestamp: Long = System.currentTimeMillis(),
)