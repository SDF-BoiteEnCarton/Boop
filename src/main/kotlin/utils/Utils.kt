package me.clcondorcet.boiteaoutils.utils

import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.math.util.ChunkUtil
import com.hypixel.hytale.protocol.BlockPosition
import com.hypixel.hytale.protocol.MovementStates
import com.hypixel.hytale.protocol.SoundCategory
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent
import com.hypixel.hytale.server.core.modules.entity.EntityModule
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.world.SoundUtil
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import me.clcondorcet.boiteaoutils.utils.interactions.CheckFalse

val ItemContainerStateType: ComponentType<ChunkStore, ItemContainerState>? = BlockStateModule.get().getComponentType(ItemContainerState::class.java)

operator fun BlockPosition.plus(b: BlockPosition): BlockPosition {
    return BlockPosition(this.x + b.x, this.y + b.y, this.z + b.z)
}

operator fun BlockPosition.minus(b: BlockPosition): BlockPosition {
    return BlockPosition(this.x - b.x, this.y - b.y, this.z - b.z)
}

// TODO Use message with translation instead
@Deprecated("Use message with translation instead")
fun Player.sendMessage(message: String) {
    sendMessage(Message.raw(message))
}

fun Ref<EntityStore>.getTransform(store: Store<EntityStore>): TransformComponent? {
    return store.getComponent(this, EntityModule.get().transformComponentType)
}

fun Player.getTransform(): TransformComponent? {
    return reference?.let { this.world?.entityStore?.store?.getComponent(it, EntityModule.get().transformComponentType)}
}

fun Player.getMovementStates(): MovementStatesComponent? {
    return reference?.let { this.world?.entityStore?.store?.getComponent(it, MovementStatesComponent.getComponentType())}
}

fun Player.isCrouching(): Boolean? {
    return getMovementStates()?.movementStates?.crouching
}

fun Player.playSoundNear(soundKey: String, volume: Float = 1F, pitch: Float = 1F) {
    val index: Int = SoundEvent.getAssetMap().getIndex(soundKey)
    world?.execute {
        val transform: TransformComponent = getTransform() ?: return@execute
        world?.entityStore?.store?.let { SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, transform.position.x, transform.position.y, transform.position.z, volume, pitch, it) }
    }
}

fun Player.playSoundSelf(soundKey: String, volume: Float = 1F, pitch: Float = 1F) {
    val index: Int = SoundEvent.getAssetMap().getIndex(soundKey)
    world?.execute {
        val transform: TransformComponent = getTransform() ?: return@execute
        world?.entityStore?.store?.let { SoundUtil.playSoundEvent3d(index, SoundCategory.SFX, transform.position.x, transform.position.y, transform.position.z, volume, pitch, { ear -> ear == reference }, it) }
    }
}

fun World.getBlockRef(blockPosition: BlockPosition): Ref<ChunkStore>? = getBlockRef(blockPosition.x, blockPosition.y, blockPosition.z)

fun World.getBlockRef(x: Int, y: Int, z: Int): Ref<ChunkStore>? {
    val chunk = getChunk(ChunkUtil.indexChunkFromBlock(x, z))
    return chunk?.blockComponentChunk?.getEntityReference(ChunkUtil.indexBlockInColumn(x, y, z))
}

fun <T : Component<ChunkStore>> Ref<ChunkStore>.getComponent(componentType: ComponentType<ChunkStore, T>): T? {
    return this.store.getComponent(this, componentType)
}
