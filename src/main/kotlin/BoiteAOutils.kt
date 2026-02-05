package me.clcondorcet.boiteaoutils

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import me.clcondorcet.boiteaoutils.commands.BoiteCommand
import me.clcondorcet.boiteaoutils.interactions.Distribute
import me.clcondorcet.boiteaoutils.packets.HidingCardsPacketHandler

class BoiteAOutils(init: JavaPluginInit) : JavaPlugin(init) {

    companion object {
        lateinit var plugin: JavaPlugin
    }

    init {
        plugin = this
    }

    override fun setup() {
        commandRegistry.registerCommand(BoiteCommand("boite", "Commandes de la boite à outils !"))
        getCodecRegistry(Interaction.CODEC)
            .register("boiteaoutils_distribute", Distribute::class.java, Distribute.CODEC)
        HidingCardsPacketHandler().registerPacketCounters()
    }
}