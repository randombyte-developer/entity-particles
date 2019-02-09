package de.randombyte.entityparticles.data

import com.pixelmonmod.pixelmon.entities.pixelmon.Entity1Base
import de.randombyte.entityparticles.EntityParticles.Companion.PIXELMON_ID
import de.randombyte.entityparticles.EntityParticles.Companion.PIXELMON_PARTICLE_TAG_KEY
import de.randombyte.kosp.extensions.orNull
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Entity

// What a hassle to get Pixelmon and the Sponge Data API working "together"

var Entity.particleId: String?
    get() {
        return if (Sponge.getPluginManager().isLoaded(PIXELMON_ID) && this is Entity1Base) {
            val persistentData = this.pokemonData.persistentData
            if (!persistentData.hasKey(PIXELMON_PARTICLE_TAG_KEY)) {
                // has to be accessed this way because of unknown issues with Sponge & Pixelmon
                val spongeSavedParticleId = persistentData.removeSpongeSavedParticleId()
                if (spongeSavedParticleId != null) {
                    this.particleId = spongeSavedParticleId // save the ID the new way (directly with NBT)
                    spongeSavedParticleId
                } else null
            } else {
                persistentData.getString(PIXELMON_PARTICLE_TAG_KEY)
            }
        } else {
            this.get(EntityParticlesKeys.PARTICLE_ID).orNull()
        }
    }
    set(id) {
        if (Sponge.getPluginManager().isLoaded(PIXELMON_ID) && this is Entity1Base) { // is Pixelmon entity? -> then do NBT
            with(this.pokemonData.persistentData) {
                if (id == null) {
                    this.removeTag(PIXELMON_PARTICLE_TAG_KEY)
                } else {
                    this.setString(PIXELMON_PARTICLE_TAG_KEY, id)
                }
            }
        } else {
            if (id == null) { // normal entity -> Sponge data api
                this.remove(EntityParticlesKeys.PARTICLE_ID)
            } else {
                this.tryOffer(ParticleData(id = id, isActive = true))
            }
        }
    }

private fun NBTTagCompound.removeSpongeSavedParticleId(): String? {
    val manipulatorsCompoundTag = this.getCompoundTag("SpongeData").getTagList("CustomManipulators", Constants.NBT.TAG_COMPOUND)
    val manipulators = manipulatorsCompoundTag.toList()

    val particleManipulatorIndex = manipulators.indexOfFirst { (it as NBTTagCompound).getString("ManipulatorId") == "entity-particles:particle" }
    if (particleManipulatorIndex < 0) return null
    val particleManipulator = manipulators[particleManipulatorIndex]

    manipulatorsCompoundTag.removeTag(particleManipulatorIndex) // remove old sponge data api tag

    return (particleManipulator as NBTTagCompound).getCompoundTag("ManipulatorData").getString("Id")
}