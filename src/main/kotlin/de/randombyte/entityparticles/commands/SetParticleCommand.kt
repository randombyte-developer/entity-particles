package de.randombyte.entityparticles.commands

import de.randombyte.entityparticles.Config
import de.randombyte.entityparticles.EntityParticles.Companion.PARTICLE_ID_ARG
import de.randombyte.entityparticles.data.ParticleData
import de.randombyte.kosp.extensions.getWorld
import de.randombyte.kosp.extensions.orNull
import de.randombyte.kosp.extensions.toText
import de.randombyte.kosp.extensions.toUUID
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.data.key.Keys

internal class SetParticleCommand(
        private val getParticleConfig: (id: String) -> Config.Particle?
) : CommandExecutor {
    internal companion object {
        internal const val WORLD_UUID_ARG = "worldUuid"
        internal const val ENTITY_UUID_ARG = "entityUuid"
    }

    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val worldUuidString = args.getOne<String>(WORLD_UUID_ARG).get()
        val entityUuidString = args.getOne<String>(ENTITY_UUID_ARG).get()
        val particleId = args.getOne<String>(PARTICLE_ID_ARG).get()

        val world = worldUuidString.toUUID().getWorld()
                ?: throw CommandException("World '$worldUuidString' is not available!".toText())
        val entity = (world.getEntity(entityUuidString.toUUID()).orNull()
                ?: throw CommandException("Entity '$entityUuidString' in world '$world' is not available!".toText()))

        if (particleId == "nothing") {
            entity.remove(ParticleData::class.java)
            entity.offer(Keys.GLOWING, false)
            return CommandResult.success()
        }

        val particleConfig = getParticleConfig(particleId) ?: throw CommandException("Particle '$particleId' is not available!".toText())

        entity.offer(ParticleData(id = particleId, isActive = true))
        if (particleConfig.glowing) entity.offer(Keys.GLOWING, true)

        return CommandResult.success()
    }
}