package de.randombyte.entityparticles

import com.flowpowered.math.vector.Vector3d
import de.randombyte.entityparticles.Config.Particle
import de.randombyte.entityparticles.Config.Particle.Effect
import de.randombyte.kosp.extensions.red
import de.randombyte.kosp.extensions.toText
import de.randombyte.kosp.extensions.typeToken
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import ninja.leaping.configurate.objectmapping.ObjectMappingException
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.Sponge
import org.spongepowered.api.effect.particle.ParticleType
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.ItemStackSnapshot
import org.spongepowered.api.text.Text

@ConfigSerializable
internal data class Config(
        @Setting val particles: Map<String, Particle> = emptyMap<String, Particle>()
) {
    @ConfigSerializable
    internal data class Particle(
            @Setting val item: ItemStackSnapshot = ItemStackSnapshot.NONE,
            @Setting val displayName: Text = Text.EMPTY,
            @Setting val itemDescription: Text = Text.EMPTY,
            @Setting val itemEnchanted: Boolean = false,
            @Setting val effects: List<Effect> = emptyList<Effect>()
    ) {
        @ConfigSerializable
        internal data class Effect(
                @Setting val type: ParticleType = ParticleTypes.HEART,
                @Setting val quantity: Int = -1,
                @Setting val velocity: Vector3d = Vector3d.ONE.negate(),
                @Setting val offset: Vector3d = Vector3d.ONE.negate(),
                @Setting(comment = "In ticks(20 ticks = 1 second)") val interval: Int = -1
        )
    }

    constructor() : this(particles = mapOf(
            "love" to Particle(
                    item = ItemStack.of(ItemTypes.BLAZE_ROD, 1).createSnapshot(),
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

    internal companion object {
        internal fun convert(configurationLoader: ConfigurationLoader<CommentedConfigurationNode>) {
            val rootNode = configurationLoader.load()
            rootNode.getNode("particles").childrenMap.forEach { _, particleNode ->
                val itemTypeNode = particleNode.getNode("item-type")
                if (!itemTypeNode.isVirtual) {
                    val itemTypeString = itemTypeNode.string
                    val itemType = Sponge.getRegistry().getType(ItemType::class.java, itemTypeString).orElseThrow {
                        ObjectMappingException("ItemType '$itemTypeString' is not available!")
                    }
                    val itemStackSnapshotNode = particleNode.getNode("item")
                    val itemStackSnapshot = ItemStack.of(itemType, 1).createSnapshot()
                    itemStackSnapshotNode.setValue(ItemStackSnapshot::class.typeToken, itemStackSnapshot)
                }
            }

            configurationLoader.save(rootNode)
        }
    }
}