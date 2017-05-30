package de.randombyte.entityparticles.data

import com.google.common.reflect.TypeToken
import de.randombyte.kosp.extensions.typeToken
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.key.KeyFactory
import org.spongepowered.api.data.value.mutable.Value

object EntityParticlesKeys {
    val PARTICLE_ID: Key<Value<String>> = KeyFactory.makeSingleKey(
            String::class.typeToken,
            object : TypeToken<Value<String>>() {},
            DataQuery.of("Id"), "entity-particles:id", "Id")

    val ACTIVE: Key<Value<Boolean>> = KeyFactory.makeSingleKey(
            Boolean::class.typeToken,
            object : TypeToken<Value<Boolean>>() {},
            DataQuery.of("Active"), "entity-particles:active", "Active")

    val IS_REMOVER: Key<Value<Boolean>> = KeyFactory.makeSingleKey(
            Boolean::class.typeToken,
            object : TypeToken<Value<Boolean>>() {},
            DataQuery.of("Remover"), "entity-particles:remover", "Remover")
}