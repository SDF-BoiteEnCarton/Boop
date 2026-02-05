package me.clcondorcet.boiteaoutils.interactions

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Holder
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.protocol.BlockPosition
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import me.clcondorcet.boiteaoutils.utils.*
import me.clcondorcet.boiteaoutils.utils.interactions.CheckFalse
import me.clcondorcet.boiteaoutils.utils.interactions.KotlinSimpleInstantInteraction
import me.clcondorcet.boiteaoutils.utils.interactions.KotlinSimpleInstantInteractionRan
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class Distribute : KotlinSimpleInstantInteraction() {

    val LOGGER: HytaleLogger = HytaleLogger.forEnclosingClass()

    companion object {
        val CODEC: BuilderCodec<Distribute?> = BuilderCodec.builder(
            Distribute::class.java, { Distribute() }, SimpleInstantInteraction.CODEC
        ).build()
    }

    override var interaction: KotlinSimpleInstantInteractionRan.() -> Unit = {
        // Checking Interaction feasibility
        val itemContainerStateType = ItemContainerStateType.asNonNull("itemContainerStateType not found", true)

        // Getting the container under the distributor
        val underBlockPos = targetBlock - BlockPosition(0, 1, 0)
        val container: ItemContainerState? = world.getBlockRef(underBlockPos)?.getComponent(itemContainerStateType)

        if (container == null) {
            player.sendMessage("There is no container under the distributor!")
            throw CheckFalse()
        }

        var simpleItemContainer = container.itemContainer as SimpleItemContainer

        // Checking action
        val crouching = player.isCrouching() ?: false

        if (crouching) {
            // Sending held item to container

            val item = context.heldItem
            if (item == null) {
                player.sendMessage("You don't have any items in your hand")
                player.playSoundSelf("SFX_Incorrect_Tool")
                throw CheckFalse()
            }

            val itemToRemove = item.withQuantity(1)!! // null is when quantity = 0
            if (!simpleItemContainer.canAddItemStack(itemToRemove)) {
                player.sendMessage("The container is full!")
                player.playSoundSelf("SFX_Incorrect_Tool")
                throw CheckFalse()
            }

            context.heldItemContainer?.moveItemStackFromSlot(context.heldItemSlot.toShort(), 1, container?.itemContainer)
            player.playSoundNear("SFX_Drop_Items_Chest")
        } else {
            // Getting random item to container

            if(simpleItemContainer.isEmpty) {
                player.sendMessage(Message.raw("The container is empty!"))
                player.playSoundNear("SFX_Incorrect_Tool")
                throw CheckFalse()
            }

            val weightedOrderedList = WeightOrderedList<Short>()

            val items = mutableMapOf<Short, ItemStack>()
            simpleItemContainer.forEach { slot, stack ->
                if (!stack.isEmpty) items[slot] = stack
            }

            weightedOrderedList.addAll(items.keys) { items[it]?.quantity ?: 0 }

            var randomItemSlot = weightedOrderedList.random()

            var transaction = simpleItemContainer.moveItemStackFromSlot(randomItemSlot, 1, player.inventory.hotbar)
            if (!transaction.succeeded()) {
                transaction = simpleItemContainer.moveItemStackFromSlot(randomItemSlot, 1, player.inventory.storage)
            }

            if(!transaction.succeeded()) {
                player.sendMessage("You are full!")
                player.playSoundSelf("SFX_Incorrect_Tool")
                throw CheckFalse()
            }

            player.playSoundNear("SFX_Axe_Crude_Swing", volume = 3F, pitch = 8F)
        }
    }
}
