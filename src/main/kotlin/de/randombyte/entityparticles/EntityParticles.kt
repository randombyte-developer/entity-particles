package de.randombyte.entityparticles

import com.google.inject.Inject
import de.randombyte.entityparticles.commands.*
import de.randombyte.entityparticles.commands.SetParticleCommand.Companion.ENTITY_UUID_ARG
import de.randombyte.entityparticles.commands.SetParticleCommand.Companion.WORLD_UUID_ARG
import de.randombyte.entityparticles.data.EntityParticlesKeys
import de.randombyte.entityparticles.data.EntityParticlesKeys.IS_REMOVER
import de.randombyte.entityparticles.data.EntityParticlesKeys.PARTICLE_ID
import de.randombyte.entityparticles.data.ParticleData
import de.randombyte.entityparticles.data.RemoverItemData
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
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Cancellable
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.InteractBlockEvent
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.NamedCause
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.filter.Getter
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.event.item.inventory.UseItemStackEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.scheduler.Task

@Plugin(id = EntityParticles.ID,
        name = EntityParticles.NAME,
        version = EntityParticles.VERSION,
        authors = arrayOf(EntityParticles.AUTHOR))
class EntityParticles @Inject constructor(
        val logger: Logger,
        @DefaultConfig(sharedRoot = true) configLoader: ConfigurationLoader<CommentedConfigurationNode>,
        val bStats: BStats
) {
    internal companion object {
        const val ID = "entity-particles"
        const val NAME = "EntityParticles"
        const val VERSION = "1.3"
        const val AUTHOR = "RandomByte"

        const val ROOT_PERMISSION = ID

        const val PARTICLE_ID_ARG = "particleId"
        const val PLAYER_ARG = "player"
    }

    private val configManager = ConfigManager(
            configLoader = configLoader,
            clazz = Config::class.java,
            hyphenSeparatedKeys = true,
            simpleTextSerialization = true,
            simpleTextTemplateSerialization = true)

    private lateinit var config: Config

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        Sponge.getDataManager().register(
                ParticleData::class.java,
                ParticleData.Immutable::class.java,
                ParticleData.Builder())

        Sponge.getDataManager().register(
                RemoverItemData::class.java,
                RemoverItemData.Immutable::class.java,
                RemoverItemData.Builder())
    }

    /**
     * All the config stuff(convert, generate, commands and the task) has to be this late to let
     * Sponge load all the DataManipulators.
     */
    @Listener
    fun onGameLoadComplete(event: GameLoadCompleteEvent) {
        Config.convert(configManager.configLoader)
        loadConfig()
        registerCommands()
        startParticleTask()

        logger.info("$NAME loaded: $VERSION")
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        loadConfig()
        registerCommands()

        logger.info("Reloaded!")
    }

    private fun loadConfig() {
        config = configManager.get()
        saveConfig() // generate config
    }

    private fun saveConfig() {
        configManager.save(config)
    }

    @Listener
    fun onRightClickEntity(event: InteractEntityEvent.Secondary.MainHand, @First player: Player, @Getter("getTargetEntity") targetEntity: Entity) {
        if (targetEntity is Player) return
        val itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orNull() ?: return

        val particleId = itemStack.get(PARTICLE_ID)?.orNull()
        val isRemover = itemStack.get(IS_REMOVER).orElse(false)

        if (particleId != null) {
            player.setItemInHand(HandTypes.MAIN_HAND, null)
            executeAsConsole("entityParticles set ${targetEntity.location.extent.uniqueId} ${targetEntity.uniqueId} $particleId")
        } else if (isRemover) {
            player.setItemInHand(HandTypes.MAIN_HAND, null)
            executeAsConsole("entityParticles set ${targetEntity.location.extent.uniqueId} ${targetEntity.uniqueId} nothing")
        }
    }

    @Listener
    fun onPlaceParticleItem(event: InteractBlockEvent.Secondary.MainHand, @First player: Player) = onUseItem(event, player)

    @Listener
    fun onUseItemEvent(event: UseItemStackEvent.Start, @First player: Player) = onUseItem(event, player)

    private fun onUseItem(event: Cancellable, player: Player) {
        val item = player.getItemInHand(HandTypes.MAIN_HAND).orNull() ?: return
        if (item.get(EntityParticlesKeys.PARTICLE_ID)?.isPresent ?: false || item.get(EntityParticlesKeys.IS_REMOVER)?.isPresent ?: false) {
            event.isCancelled = true
            player.sendMessage("You can't use a ParticleItem!".red())
        }
    }

    private fun registerCommands() {
        Sponge.getCommandManager().getOwnedBy(this).forEach { Sponge.getCommandManager().removeMapping(it) }

        val particleIdChoices = config.particles.keys.map { it to it }.toMap()

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .child(CommandSpec.builder()
                        .permission("$ROOT_PERMISSION.give")
                        .arguments(
                                playerOrSource(PLAYER_ARG.toText()),
                                choices(PARTICLE_ID_ARG.toText(), particleIdChoices))
                        .executor(GiveParticleItemCommand(
                                getParticle = { id -> config.particles[id] },
                                cause = Cause.of(NamedCause.source(this))))
                        .build(), "give")
                .child(CommandSpec.builder()
                        .permission("$ROOT_PERMISSION.set")
                        .arguments(
                                string(WORLD_UUID_ARG.toText()),
                                string(ENTITY_UUID_ARG.toText()),
                                choices(PARTICLE_ID_ARG.toText(), particleIdChoices.plus("nothing" to "nothing")))
                        .executor(SetParticleCommand(
                                particleExists = { id -> config.particles.containsKey(id) }))
                        .build(), "set")
                .child(CommandSpec.builder()
                        .permission("$ROOT_PERMISSION.newConfig")
                        .arguments(string(PARTICLE_ID_ARG.toText()))
                        .executor(NewParticleConfigCommand(
                                addNewConfig = { id, particle ->
                                    config = config.copy(particles = config.particles + (id to particle))
                                    saveConfig()
                                },
                                updateCommands = { registerCommands() }
                        ))
                        .build(), "newConfig")
                .child(CommandSpec.builder()
                        .child(CommandSpec.builder()
                                .permission("$ROOT_PERMISSION.removerItem.set")
                                .executor(SetRemoverItemCommand(
                                        setItemStackSnapshot = { removerItemStackSnapshot ->
                                            config = config.copy(removerItem = removerItemStackSnapshot)
                                            saveConfig()
                                        }
                                ))
                                .build(), "set")
                        .child(CommandSpec.builder()
                                .permission("$ROOT_PERMISSION.removerItem.give")
                                .arguments(playerOrSource(PLAYER_ARG.toText()))
                                .executor(GiveRemoverItemCommand(
                                        cause = Cause.of(NamedCause.source(this)),
                                        getRemoverItem = { config.removerItem }
                                ))
                                .build(), "give")
                        .build(), "removerItem")
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
                                    val particleConfig = config.particles[particleId]
                                    if (particleConfig == null) {
                                        // invalid data -> remove
                                        entity.remove(ParticleData::class.java)
                                        return@entityLoop
                                    }

                                    particleConfig.effects.forEach { effect ->
                                        val doEffectThisTick = Sponge.getServer().runningTimeTicks % effect.interval == 0
                                        if (doEffectThisTick) {
                                            entity.spawnParticles(effect)
                                            entity.offer(Keys.GLOWING, particleConfig.glowing)
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