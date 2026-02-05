package me.clcondorcet.boiteaoutils.utils.interactions

import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.protocol.BlockPosition
import com.hypixel.hytale.protocol.InteractionState
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class CheckFalse : RuntimeException()

data class KotlinSimpleInstantInteractionRan (
    val type: InteractionType,
    val context: InteractionContext,
    val cooldownHandler: CooldownHandler,
) {
    /*
    // Easy Getters nullable
     */

    fun getCommandBufferNullable(): CommandBuffer<EntityStore>? = context.commandBuffer

    fun getPlayerNullable(): Player? {
        val cmdBuffer = getCommandBufferNullable() ?: return null
        val playerRef: Ref<EntityStore> = context.entity
        return cmdBuffer.getComponent(playerRef, Player.getComponentType())
    }

    fun getWorldNullable(): World? {
        val cmdBuffer = getCommandBufferNullable() ?: return null
        return cmdBuffer.getExternalData().world
    }

    fun getTargetBlockNullable(): BlockPosition? {
        return context.targetBlock
    }

    fun getStoreNullable(): Store<EntityStore>? {
        val cmdBuffer = getCommandBufferNullable() ?: return null
        return cmdBuffer.store
    }

    /*
    // Getters non-null with throw customizable
     */

    internal var _commandBufer: CommandBuffer<EntityStore>? = null
    fun getCommandBuffer(message: String? = null): CommandBuffer<EntityStore> {
        _commandBufer?.let { return it }
        val cmdBuffer = getCommandBufferNullable()
        if (cmdBuffer == null) failed(message)
        _commandBufer = cmdBuffer
        return cmdBuffer!!
    }

    internal var _player: Player? = null
    fun getPlayer(message: String? = null, propagate: Boolean = true): Player {
        _player?.let { return it }
        val cmdBuffer = if (propagate) getCommandBuffer(message) else commandBuffer
        val playerRef: Ref<EntityStore> = context.entity
        val player = cmdBuffer.getComponent(playerRef, Player.getComponentType())
        if(player == null) failed(message)
        _player = player
        return player!!
    }

    internal var _world: World? = null
    fun getWorld(message: String? = null, propagate: Boolean = true): World {
        _world?.let { return it }
        val cmdBuffer = if (propagate) getCommandBuffer(message) else commandBuffer
        val world = cmdBuffer.getExternalData().world
        _world = world
        return world
    }

    internal var _targetBlock: BlockPosition? = null
    fun getTargetBlock(message: String? = null, sendToPlayer: Boolean = false): BlockPosition {
        _targetBlock?.let { return it }
        val targetBlock = getTargetBlockNullable()
        if(targetBlock == null) failed(message, sendToPlayer)
        _targetBlock = targetBlock
        return targetBlock!!
    }

    internal var _store: Store<EntityStore>? = null
    fun getStore(message: String? = null, sendToPlayer: Boolean = false, propagate: Boolean = true): Store<EntityStore> {
        _store?.let { return it }
        val cmdBuffer = if (propagate) getCommandBuffer(message) else commandBuffer
        val store = cmdBuffer.store
        _store = store
        return store
    }

    /*
    // Easy Getters non-null with throw
     */

    val commandBuffer = getCommandBuffer("Interaction failed! Command Buffer is null.")
    val player = getPlayer("Interaction failed! Player is null.", propagate = false)
    val world = getWorld("Interaction failed! World is null.", propagate = false)
    val targetBlock = getTargetBlock("Interaction failed! Target block is null.")
    val store = getStore("Interaction failed! Store is null.", sendToPlayer = false)
    val entityRef = context.entity

    /*
    // Thrower
     */

    fun failed(message: String? = null, sendToPlayer: Boolean = false) {
        context.state.state = InteractionState.Failed
        message ?: HytaleLogger.forEnclosingClass().atInfo().log(message)
        if (sendToPlayer) message?.let { getPlayerNullable()?.sendMessage(Message.raw(it)) }
        throw CheckFalse()
    }

    fun <T> T?.asNonNull(message: String? = null, sendToPlayer: Boolean = false): T {
        if (this == null) failed(message, sendToPlayer)
        return this!!
    }
}

abstract class KotlinSimpleInstantInteraction(): SimpleInstantInteraction() {
    abstract var interaction: KotlinSimpleInstantInteractionRan.() -> Unit

    override fun firstRun(type: InteractionType, context: InteractionContext, cooldownHandler: CooldownHandler) {
        try {
            val runner = KotlinSimpleInstantInteractionRan(type, context, cooldownHandler)
            interaction.invoke(runner)
        } catch (_: CheckFalse) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
