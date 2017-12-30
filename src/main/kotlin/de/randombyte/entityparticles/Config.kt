package de.randombyte.entityparticles

import com.flowpowered.math.vector.Vector3d
import com.flowpowered.math.vector.Vector3i
import de.randombyte.entityparticles.Config.Particle.Effect
import de.randombyte.entityparticles.data.ParticleData
import de.randombyte.kosp.extensions.red
import de.randombyte.kosp.extensions.toText
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleType
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.entity.EntityType
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.item.enchantment.Enchantment
import org.spongepowered.api.item.enchantment.EnchantmentTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.text.Text

@ConfigSerializable
internal data class Config(
        @Setting(comment = "Ignore the 'glowing' and 'effects' setting.") val removerItem: Particle = Particle(),
        @Setting val particles: Map<String, Particle> = emptyMap(),
        @Setting val blockedEntities: List<EntityType> = emptyList()
) {
    @ConfigSerializable
    internal data class Particle(
            @Setting val item: String = "",
            @Setting val displayName: Text = Text.EMPTY,
            @Setting val itemDescription: Text = Text.EMPTY,
            @Setting val itemEnchanted: Boolean = false,
            @Setting val glowing: Boolean = false,
            @Setting val effects: List<Effect> = emptyList()
    ) {
        @ConfigSerializable
        internal data class Effect(
                @Setting val type: ParticleType = ParticleTypes.HEART,
                @Setting val quantity: Int = -1,
                @Setting val velocity: Vector3d = Vector3d.ONE.negate(),
                @Setting val offset: Vector3d = Vector3d.ONE.negate(),
                @Setting val centerOffset: Vector3d = Vector3d.ZERO,
                @Setting(comment = "In ticks(20 ticks = 1 second)") val interval: Int = -1,
                @Setting(comment = "Supported by redstone dust") val color: Vector3i = Vector3i.ONE.negate()
        )

        fun createItemStack() = ItemStack.builder()
                .fromSnapshot(resolveByteItems(item))
                .quantity(1) // force single item
                .apply {
                    if (itemEnchanted) {
                        add(Keys.ITEM_ENCHANTMENTS, listOf(Enchantment.of(EnchantmentTypes.LUCK_OF_THE_SEA, 0)))
                        add(Keys.HIDE_ENCHANTMENTS, true)
                    }
                }
                .add(Keys.DISPLAY_NAME, displayName)
                .add(Keys.ITEM_LORE, listOf(itemDescription))
                .build()
    }

    constructor() : this(
            blockedEntities = listOf(EntityTypes.PLAYER),
            removerItem = Particle(
                    item = "minecraft:bone",
                    displayName = "Particles remover".toText(),
                    itemDescription = Text.EMPTY,
                    itemEnchanted = true
            ),
            particles = mapOf(
            "love" to Particle(
                    item = "minecraft:blaze_rod",
                    displayName = "Love".red(),
                    itemDescription = "Right click an entity to apply this effect".toText(),
                    itemEnchanted = true,
                    glowing = false,
                    effects = listOf(Effect(
                            type = ParticleTypes.HEART,
                            quantity = 10,
                            velocity = Vector3d(0.0, 0.3, 0.0),
                            offset = Vector3d.ONE,
                            interval = 20,
                            color = Vector3i.ONE
                    )))
    ))
}