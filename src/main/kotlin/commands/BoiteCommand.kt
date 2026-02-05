package me.clcondorcet.boiteaoutils.commands

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.command.system.CommandContext
import java.util.concurrent.CompletableFuture

class BoiteCommand(name: String, description: String) : AbstractCommand(name, description) {
    override fun execute(context: CommandContext): CompletableFuture<Void>? {
        context.sendMessage(Message.raw("La boite est ouverte !"))
        return CompletableFuture.completedFuture(null)
    }
}