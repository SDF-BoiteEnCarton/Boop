package me.clcondorcet.boiteaoutils.packets

import com.hypixel.hytale.protocol.*
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates
import com.hypixel.hytale.protocol.packets.interaction.PlayInteractionFor
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains
import com.hypixel.hytale.server.core.asset.type.item.config.Item
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe

class HidingCardsPacketHandler {
    fun registerPacketCounters() {
        PacketAdapters.registerOutbound (PlayerPacketWatcher { ref: PlayerRef, packet: Packet? ->
            if (packet is EntityUpdates || packet is SyncInteractionChains || packet is PlayInteractionFor) {
                val worldUUID = ref.worldUuid ?: return@PlayerPacketWatcher
                val networkId = ref.reference?.let { Universe.get().getWorld(worldUUID)?.entityStore?.store?.getComponent(it, NetworkId.getComponentType()) }
                if (packet is EntityUpdates) {
                    packet.updates?.forEach { update ->
                        if (networkId?.id != update.networkId) {
                            update?.updates?.clone()?.forEach { entityUpdate ->
                                val itemId = entityUpdate.equipment?.rightHandItemId ?: return@forEach
                                Item.getAssetMap()?.getAsset(itemId)?.let { asset ->
                                    if (asset.data.rawTags.contains("Hidden")) {
                                        val modifiedObj = entityUpdate.clone()
                                        modifiedObj.equipment?.rightHandItemId =
                                            asset.data.rawTags["Hidden"]?.first() ?: return@forEach
                                        val newArray = update.updates!!.toMutableList()
                                        newArray.remove(entityUpdate)
                                        newArray.add(modifiedObj)
                                        update.updates = newArray.toTypedArray()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}
