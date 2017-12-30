package de.randombyte.entityparticles

import de.randombyte.byteitems.ByteItemsApi
import de.randombyte.kosp.getServiceOrFail
import org.spongepowered.api.item.inventory.ItemStack

internal fun ItemStack.singleCopy() = copy().apply { quantity = 1 }

internal fun resolveByteItems(item: String) = getServiceOrFail(ByteItemsApi::class).getItemSafely(item)