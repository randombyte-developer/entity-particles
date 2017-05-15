package de.randombyte.entityparticles

import com.google.inject.Inject
import de.randombyte.entitycommands.data.EntityParticlesKeys
import de.randombyte.entityparticles.commands.GiveCommand
import de.randombyte.entityparticles.commands.GiveCommand.Companion.PLAYER_ARG
import de.randombyte.entityparticles.commands.SetCommand
import de.randombyte.entityparticles.commands.SetCommand.Companion.ENTITY_UUID_ARG
import de.randombyte.entityparticles.commands.SetCommand.Companion.WORLD_UUID_ARG
import de.randombyte.entityparticles.data.ParticleData
import de.randombyte.kosp.bstats.BStats
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.executeAsConsole
import de.randombyte.kosp.extensions.orNull
import de.randombyte.kosp.extensions.red
import de.randombyte.kosp.extensions.toText
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments.*
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.InteractBlockEvent
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.NamedCause
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.filter.Getter
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task

@Plugin(id = EntityParticles.ID,
        name = EntityParticles.NAME,
        version = EntityParticles.VERSION,
        authors = arrayOf(EntityParticles.AUTHOR))
class EntityParticles @Inject constructor(
        val logger: Logger,
        @DefaultConfig(sharedRoot = true) configLoader: ConfigurationLoader<CommentedConfigurationNode>,
        val bStats: BStats,
        val pluginContainer: PluginContainer
) {
    internal companion object {
        const val ID = "entity-particles"
        const val NAME = "EntityParticles"
        const val VERSION = "1.0"
        const val AUTHOR = "RandomByte"

        const val ROOT_PERMISSION = ID

        const val PARTICLE_ID_ARG = "particleId"
    }

    private val configManager = ConfigManager(
            configLoader = configLoader,
            clazz = Config::class.java,
            hyphenSeparatedKeys = true,
            simpleTextSerialization = true,
            simpleTextTemplateSerialization = true)

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        Sponge.getDataManager().register(
                ParticleData::class.java,
                ParticleData.Immutable::class.java,
                ParticleData.Builder())
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        Config.convert(configManager.configLoader)
        generateConfig()
        registerCommands()
        startParticleTask()

        logger.info("$NAME loaded: $VERSION")
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        generateConfig()
        registerCommands()

        logger.info("Reloaded!")
    }

    private fun generateConfig() = configManager.save(configManager.get())

    @Listener
    fun onRightClickEntity(event: InteractEntityEvent.Secondary.MainHand, @First player: Player, @Getter("getTargetEntity") targetEntity: Entity) {
        val itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orNull()
        val particleId = itemStack?.get(EntityParticlesKeys.PARTICLE_ID)?.orNull() ?: return
        if (targetEntity is Player) return

        player.setItemInHand(HandTypes.MAIN_HAND, null)
        executeAsConsole("entityParticles set ${targetEntity.location.extent.uniqueId} ${targetEntity.uniqueId} $particleId")
    }

    @Listener
    fun onPlaceParticleItem(event: InteractBlockEvent.Secondary.MainHand, @First player: Player) {
        if (player.getItemInHand(HandTypes.MAIN_HAND).orNull()?.get(EntityParticlesKeys.PARTICLE_ID)?.isPresent ?: false) {
            event.isCancelled = true
            event.cause.first(Player::class.java).orNull()?.sendMessage("You can't place a ParticleItem!".red())
        }
    }

    private fun registerCommands() {
        Sponge.getCommandManager().getOwnedBy(this).forEach { Sponge.getCommandManager().removeMapping(it) }

        val particleIdChoices = configManager.get().particles.keys.map { it to it }.toMap()

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .child(CommandSpec.builder()
                        .permission("$ROOT_PERMISSION.give")
                        .arguments(
                                playerOrSource(PLAYER_ARG.toText()),
                                choices(PARTICLE_ID_ARG.toText(), particleIdChoices))
                        .executor(GiveCommand(
                                getParticle = { id -> configManager.get().particles[id] },
                                cause = Cause.of(NamedCause.source(this))))
                        .build(), "give")
                .child(CommandSpec.builder()
                        .permission("$ROOT_PERMISSION.set")
                        .arguments(
                                string(WORLD_UUID_ARG.toText()),
                                string(ENTITY_UUID_ARG.toText()),
                                choices(PARTICLE_ID_ARG.toText(), particleIdChoices))
                        .executor(SetCommand(
                                particleExists = { id -> configManager.get().particles.containsKey(id) }))
                        .build(), "set")
                .build(), "entityParticles", "particles", "ep")
    }

    private fun startParticleTask() {
        Task.builder()
                .intervalTicks(1)
                .execute { ->
                    Sponge.getServer().worlds.forEach { world ->
                        world.entities
                                .filter { it.get(EntityParticlesKeys.PARTICLE_ID).isPresent }
                                .forEach entityLoop@ { entity ->
                                    val particleId = entity.get(EntityParticlesKeys.PARTICLE_ID).get()
                                    val particle = configManager.get().particles[particleId]
                                    if (particle == null) {
                                        // invalid data -> remove
                                        entity.remove(ParticleData::class.java)
                                        return@entityLoop
                                    }

                                    particle.effects.forEach { effect ->
                                        val doEffectThisTick = Sponge.getServer().runningTimeTicks % effect.interval == 0
                                        if (doEffectThisTick) {
                                            entity.spawnParticles(effect)
                                        }
                                    }
                                }
                    }
                }
                .submit(this)
    }

    private fun Entity.spawnParticles(effect: Config.Particle.Effect) {
        val particleEffect = ParticleEffect.builder()
                .type(effect.type)
                .quantity(effect.quantity)
                .velocity(effect.velocity)
                .offset(effect.offset)
                .build()
        location.extent.spawnParticles(particleEffect, location.position)
    }
}