package de.randombyte.entityparticles.data

import de.randombyte.entityparticles.EntityParticles
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.util.TypeTokens

object EntityParticlesKeys {
    lateinit var PARTICLE_ID: Key<Value<String>>

    lateinit var ACTIVE: Key<Value<Boolean>>

    lateinit var IS_REMOVER: Key<Value<Boolean>>

    fun buildKeys() {
        PARTICLE_ID = Key.builder()
                .type(TypeTokens.STRING_VALUE_TOKEN)
                .id("${EntityParticles.ID}:id")
                .name("ID")
                .query(DataQuery.of("Id"))
                .build()

        ACTIVE = Key.builder()
                .type(TypeTokens.BOOLEAN_VALUE_TOKEN)
                .id("${EntityParticles.ID}:active")
                .name("Active")
                .query(DataQuery.of("Active"))
                .build()

        IS_REMOVER = Key.builder()
                .type(TypeTokens.BOOLEAN_VALUE_TOKEN)
                .id("${EntityParticles.ID}:remover")
                .name("Remover")
                .query(DataQuery.of("Remover"))
                .build()
    }
}