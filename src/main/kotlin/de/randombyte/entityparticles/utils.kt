package de.randombyte.entityparticles

import org.spongepowered.api.item.inventory.ItemStack

internal fun ItemStack.singleCopy() = copy().apply { quantity = 1 }