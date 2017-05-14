package de.randombyte.entityparticles

import com.flowpowered.math.vector.Vector3d
import de.randombyte.entityparticles.Config.Particle
import de.randombyte.entityparticles.Config.Particle.Effect
import de.randombyte.kosp.extensions.red
import de.randombyte.kosp.extensions.toText
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.effect.particle.ParticleType
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.text.Text

@ConfigSerializable
internal class Config(
        @Setting val particles: Map<String, Particle> = emptyMap<String, Particle>()
) {
    @ConfigSerializable
    class Particle(
            @Setting val itemType: ItemType = ItemTypes.NONE,
            @Setting val displayName: Text = Text.EMPTY,
            @Setting val itemDescription: Text = Text.EMPTY,
            @Setting val itemEnchanted: Boolean = false,
            @Setting val effects: List<Effect> = emptyList<Effect>()
    ) {
        @ConfigSerializable
        class Effect(
                @Setting val type: ParticleType = ParticleTypes.HEART,
                @Setting val quantity: Int = -1,
                @Setting val velocity: Vector3d = Vector3d.ONE.negate(),
                @Setting val offset: Vector3d = Vector3d.ONE.negate(),
                @Setting(comment = "In ticks(20 ticks = 1 second)") val interval: Int = -1
        )
    }

    constructor() : this(particles = mapOf(
            "love" to Particle(
                    itemType = ItemTypes.BLAZE_ROD,
                    displayName = "Love".red(),
                    itemDescription = "Right click an entity to apply this effect".toText(),
                    itemEnchanted = true,
                    effects = listOf(Effect(
                            type = ParticleTypes.HEART,
                            quantity = 10,
                            velocity = Vector3d(0.0, 0.3, 0.0),
                            offset = Vector3d.ONE,
                            interval = 20
                    )))
    ))
}