package de.randombyte.entityparticles.commands

import de.randombyte.entityparticles.Config
import de.randombyte.entityparticles.EntityParticles.Companion.PARTICLE_ID_ARG
import de.randombyte.entityparticles.data.ParticleData
import de.randombyte.kosp.extensions.green
import de.randombyte.kosp.extensions.toText
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.data.meta.ItemEnchantment
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.item.Enchantments
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult

internal class GiveCommand(
        private val getParticle: (id: String) -> Config.Particle?,
        private val cause: Cause
): CommandExecutor {
    internal companion object {
        internal const val PLAYER_ARG = "player"
    }

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val player = args.getOne<Player>(PLAYER_ARG).get()
        val particleId = args.getOne<String>(PARTICLE_ID_ARG).get()

        val particle = getParticle(particleId)
                ?: throw CommandException("Particle '$particleId' is not available!".toText())

        val itemStack = ItemStack.builder()
                .fromSnapshot(particle.item)
                .quantity(1) // force single item
                .apply {
                    if (particle.itemEnchanted) {
                        add(Keys.ITEM_ENCHANTMENTS, listOf(ItemEnchantment(Enchantments.BANE_OF_ARTHROPODS, 1)))
                        add(Keys.HIDE_ENCHANTMENTS, true)
                    }
                }
                .add(Keys.DISPLAY_NAME, particle.displayName)
                .add(Keys.ITEM_LORE, listOf(particle.itemDescription))
                .itemData(ParticleData(id = particleId, isActive = false))
                .build()

        player.give(itemStack, cause)

        player.sendMessage("Given '$particleId' to ${player.name}!".green())

        return CommandResult.success()
    }

    /**
     * Gives the [Player] the [itemStack] by: 1. putting it in the hand; 2. putting it somewhere
     * in the inventory; 3. dropping it onto the ground
     */
    private fun Player.give(itemStack: ItemStack, cause: Cause) {
        val isPlayerHoldingSomething = getItemInHand(HandTypes.MAIN_HAND).isPresent
        if (!isPlayerHoldingSomething) {
            // nothing in hand -> put item in hand
            setItemInHand(HandTypes.MAIN_HAND, itemStack)
        } else {
            // something in hand -> place item somewhere in inventory
            val transactionResult = inventory.offer(itemStack)
            if (transactionResult.type != InventoryTransactionResult.Type.SUCCESS) {
                // inventory full -> spawn as item
                val entity = location.extent.createEntity(EntityTypes.ITEM, location.position)
                entity.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot())
                if (!location.extent.spawnEntity(entity, cause)) {
                    throw CommandException("Couldn't spawn Item!".toText())
                }
            }
        }
    }
}