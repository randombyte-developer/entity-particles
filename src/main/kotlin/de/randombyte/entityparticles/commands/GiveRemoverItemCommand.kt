package de.randombyte.entityparticles.commands

import de.randombyte.entityparticles.EntityParticles.Companion.PLAYER_ARG
import de.randombyte.entityparticles.data.RemoverItemData
import de.randombyte.entityparticles.singleCopy
import de.randombyte.kosp.extensions.give
import de.randombyte.kosp.extensions.green
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.ItemStackSnapshot

internal class GiveRemoverItemCommand(
        private val getRemoverItem: () -> ItemStack
) : CommandExecutor {
    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val player = args.getOne<Player>(PLAYER_ARG).get()

        val itemStack = getRemoverItem()
                .singleCopy()
                .apply { offer(RemoverItemData(isRemover = true)) }
        player.give(itemStack)

        player.sendMessage("Given the remover item to ${player.name}!".green())

        return CommandResult.success()
    }
}