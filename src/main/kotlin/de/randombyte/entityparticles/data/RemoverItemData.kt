package de.randombyte.entityparticles.data

import de.randombyte.entitycommands.data.EntityParticlesKeys.IS_REMOVER
import de.randombyte.entityparticles.data.RemoverItemData.Immutable
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

class RemoverItemData internal constructor(var isRemover: Boolean = false) : AbstractData<RemoverItemData, Immutable>() {

    val isRemoverValue: Value<Boolean>
        get() = Sponge.getRegistry().valueFactory.createValue(IS_REMOVER, isRemover)

    init {
        registerGettersAndSetters()
    }

    override fun registerGettersAndSetters() {
        registerFieldGetter(IS_REMOVER, { isRemover })
        registerFieldSetter(IS_REMOVER, { isRemover = it })
        registerKeyValue(IS_REMOVER, { isRemoverValue })
    }

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<RemoverItemData> {
        dataHolder.get(RemoverItemData::class.java).ifPresent { that ->
            val data = overlap.merge(this, that)
            this.isRemover = data.isRemover
        }
        return Optional.of(this)
    }

    override fun from(container: DataContainer) = from(container as DataView)

    private fun from(container: DataView): Optional<RemoverItemData> {
        container.getBoolean(IS_REMOVER.query).ifPresent { isRemover = it }
        return toOptional()
    }

    override fun copy() = RemoverItemData(isRemover)

    override fun asImmutable() = Immutable(isRemover)

    override fun getContentVersion() = 1

    override fun toContainer(): DataContainer = super.toContainer()
            .set(IS_REMOVER.query, isRemover)

    class Immutable(val isRemover: Boolean) : AbstractImmutableData<Immutable, RemoverItemData>() {

        init {
            registerGetters()
        }

        override fun registerGetters() {
            registerFieldGetter(IS_REMOVER, { isRemover })
            registerKeyValue(IS_REMOVER, {
                Sponge.getRegistry().valueFactory.createValue(IS_REMOVER, isRemover).asImmutable()
            })
        }

        override fun asMutable() = RemoverItemData(isRemover)

        override fun getContentVersion() = 1

        override fun toContainer(): DataContainer = super.toContainer()
                .set(IS_REMOVER.query, isRemover)
    }

    class Builder internal constructor() : AbstractDataBuilder<RemoverItemData>(RemoverItemData::class.java, 1), DataManipulatorBuilder<RemoverItemData, Immutable> {

        override fun create() = RemoverItemData()

        override fun createFrom(dataHolder: DataHolder): Optional<RemoverItemData> = create().fill(dataHolder)

        @Throws(InvalidDataException::class)
        override fun buildContent(container: DataView) = create().from(container)
    }
}
