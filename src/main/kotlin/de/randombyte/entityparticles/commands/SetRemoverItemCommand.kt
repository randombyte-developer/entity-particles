package de.randombyte.entityparticles.commands

import de.randombyte.entityparticles.singleCopy
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.extensions.green
import de.randombyte.kosp.extensions.toText
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStackSnapshot

internal class SetRemoverItemCommand(
        private val setItemStackSnapshot: (ItemStackSnapshot) -> Unit
) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orElseThrow {
            CommandException("You must hold the item your the hand!".toText())
        }.copy()

        setItemStackSnapshot(itemStack.singleCopy().createSnapshot())

        player.sendMessage("Remover item set!".green())

        return CommandResult.success()
    }
}