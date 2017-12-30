package de.randombyte.entityparticles.commands

import de.randombyte.entityparticles.Config
import de.randombyte.entityparticles.EntityParticles.Companion.PARTICLE_ID_ARG
import de.randombyte.entityparticles.EntityParticles.Companion.PLAYER_ARG
import de.randombyte.entityparticles.data.ParticleData
import de.randombyte.kosp.extensions.give
import de.randombyte.kosp.extensions.green
import de.randombyte.kosp.extensions.toText
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.entity.living.player.Player

internal class GiveParticleItemCommand(private val getParticle: (id: String) -> Config.Particle?): CommandExecutor {
    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val player = args.getOne<Player>(PLAYER_ARG).get()
        val particleId = args.getOne<String>(PARTICLE_ID_ARG).get()

        val particle = getParticle(particleId)
                ?: throw CommandException("Particle '$particleId' is not available!".toText())

        val itemStack = particle.createItemStack()
                .apply { offer(ParticleData(id = particleId, isActive = false)) }
        player.give(itemStack)

        player.sendMessage("Given '$particleId' to ${player.name}!".green())

        return CommandResult.success()
    }
}