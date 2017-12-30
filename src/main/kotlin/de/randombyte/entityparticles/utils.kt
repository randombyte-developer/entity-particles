package de.randombyte.entityparticles

import de.randombyte.byteitems.ByteItemsApi
import de.randombyte.kosp.getServiceOrFail
import org.spongepowered.api.Sponge
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.ItemStackSnapshot

internal fun ItemStack.singleCopy() = copy().apply { quantity = 1 }

internal fun resolveByteItems(item: String): ItemStackSnapshot? {
    return if (Sponge.getPluginManager().getPlugin("byte-items").isPresent) {
        getServiceOrFail(ByteItemsApi::class).getItemSafely(item)
    } else {
        val itemType = Sponge.getRegistry().getType(ItemType::class.java, item).orElseThrow {
            IllegalArgumentException("Couldn't find ItemType '$item'!")
        }
        ItemStack.of(itemType, 1).createSnapshot()
    }
}