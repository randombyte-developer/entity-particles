package de.randombyte.entityparticles.commands

import de.randombyte.entityparticles.Config
import de.randombyte.entityparticles.EntityParticles
import de.randombyte.entityparticles.singleCopy
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.extensions.green
import de.randombyte.kosp.extensions.toText
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.living.player.Player

internal class NewParticleConfigCommand(
        private val addNewConfig: (id: String, Config.Particle) -> Unit,
        private val updateCommands: () -> Unit
) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val newId = args.getOne<String>(EntityParticles.PARTICLE_ID_ARG).get()
        val itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orElseThrow {
            CommandException("You must hold the item your the hand!".toText())
        }.copy()

        addNewConfig(newId, Config().particles.getValue("love").copy(
                item = itemStack.singleCopy().createSnapshot()
        ))

        player.sendMessage("Added to config!".green())

        updateCommands()

        return CommandResult.success()
    }
}