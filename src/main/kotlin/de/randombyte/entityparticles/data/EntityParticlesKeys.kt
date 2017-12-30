package de.randombyte.entityparticles.data

import com.google.common.reflect.TypeToken
import de.randombyte.entityparticles.EntityParticles
import de.randombyte.kosp.extensions.typeToken
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.key.KeyFactory
import org.spongepowered.api.data.value.mutable.Value

object EntityParticlesKeys {
    val PARTICLE_ID: Key<Value<String>> = Key.builder()
            .type(object : TypeToken<Value<String>>() {})
            .id("${EntityParticles.ID}:id")
            .name("ID")
            .query(DataQuery.of("Id"))
            .build()

    val ACTIVE: Key<Value<Boolean>> = Key.builder()
            .type(object : TypeToken<Value<Boolean>>() {})
            .id("${EntityParticles.ID}:acitve")
            .name("Active")
            .query(DataQuery.of("Active"))
            .build()

    val IS_REMOVER: Key<Value<Boolean>> = Key.builder()
            .type(object : TypeToken<Value<Boolean>>() {})
            .id("${EntityParticles.ID}:remover")
            .name("Remover")
            .query(DataQuery.of("Remover"))
            .build()
}