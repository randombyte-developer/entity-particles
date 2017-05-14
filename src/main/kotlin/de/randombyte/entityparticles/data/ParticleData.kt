package de.randombyte.entityparticles.data

import de.randombyte.entitycommands.data.EntityParticlesKeys.ACTIVE
import de.randombyte.entitycommands.data.EntityParticlesKeys.PARTICLE_ID
import de.randombyte.entityparticles.data.ParticleData.Immutable
import de.randombyte.kosp.extensions.toOptional
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData
import org.spongepowered.api.data.merge.MergeFunction
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import org.spongepowered.api.data.persistence.InvalidDataException
import org.spongepowered.api.data.value.mutable.Value
import java.util.*

class ParticleData internal constructor(
        var id: String = "",
        var isActive: Boolean = false
) : AbstractData<ParticleData, Immutable>() {

    val idValue: Value<String>
        get() = Sponge.getRegistry().valueFactory.createValue(PARTICLE_ID, id)

    val activeValue: Value<Boolean>
        get() = Sponge.getRegistry().valueFactory.createValue(ACTIVE, isActive)

    init {
        registerGettersAndSetters()
    }

    override fun registerGettersAndSetters() {
        registerFieldGetter(PARTICLE_ID, { id })
        registerFieldSetter(PARTICLE_ID, { id = it })
        registerKeyValue(PARTICLE_ID, { idValue })

        registerFieldGetter(ACTIVE, { isActive })
        registerFieldSetter(ACTIVE, { isActive = it })
        registerKeyValue(ACTIVE, { activeValue })
    }

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<ParticleData> {
        dataHolder.get(ParticleData::class.java).ifPresent { that ->
            val data = overlap.merge(this, that)
            this.id = data.id
            this.isActive = data.isActive
        }
        return Optional.of(this)
    }

    override fun from(container: DataContainer) = from(container as DataView)

    private fun from(container: DataView): Optional<ParticleData> {
        container.getString(PARTICLE_ID.query).ifPresent { id = it }
        container.getBoolean(ACTIVE.query).ifPresent { isActive = it }
        return toOptional()
    }

    override fun copy() = ParticleData(id, isActive)

    override fun asImmutable() = Immutable(id, isActive)

    override fun getContentVersion() = 1

    override fun toContainer(): DataContainer = super.toContainer()
            .set(PARTICLE_ID.query, id)
            .set(ACTIVE.query, isActive)

    class Immutable(
            val id: String,
            val isActive: Boolean
    ) : AbstractImmutableData<Immutable, ParticleData>() {

        init {
            registerGetters()
        }

        override fun registerGetters() {
            registerFieldGetter(PARTICLE_ID, { id })
            registerKeyValue(PARTICLE_ID, {
                Sponge.getRegistry().valueFactory.createValue(PARTICLE_ID, id).asImmutable()
            })

            registerFieldGetter(ACTIVE, { isActive })
            registerKeyValue(ACTIVE, {
                Sponge.getRegistry().valueFactory.createValue(ACTIVE, isActive).asImmutable()
            })
        }

        override fun asMutable() = ParticleData(id, isActive)

        override fun getContentVersion() = 1

        override fun toContainer(): DataContainer = super.toContainer()
                .set(PARTICLE_ID.query, id)
                .set(ACTIVE.query, isActive)
    }

    class Builder internal constructor() : AbstractDataBuilder<ParticleData>(ParticleData::class.java, 1), DataManipulatorBuilder<ParticleData, Immutable> {

        override fun create() = ParticleData()

        override fun createFrom(dataHolder: DataHolder): Optional<ParticleData> = create().fill(dataHolder)

        @Throws(InvalidDataException::class)
        override fun buildContent(container: DataView) = create().from(container)
    }
}
