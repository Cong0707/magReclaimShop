package io.cong.magReclaimShop.command

import io.cong.magReclaimShop.MagReclaimShop
import io.cong.magReclaimShop.ui.UIUtil.openShop
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggestPlayers
import taboolib.platform.util.onlinePlayers

@CommandHeader("magReclaimShop", permission = "magReclaimShop.command", aliases = ["magrs"])
@Suppress("unused")
object MagReclaimShopCommand {

    val plugin: MagReclaimShop = MagReclaimShop.plugin

    @CommandBody
    val open = subCommand {
        dynamic("ui") {
            suggestion<CommandSender> { _, _ ->
                plugin.shops.map { it.title }
            }
            dynamic("player") {
                suggestPlayers()
                execute<CommandSender> { sender, context, _ ->
                    val player = onlinePlayers.find { it.name == context["player"] } ?: return@execute
                    val ui = plugin.shops.find { it.title == context["ui"] } ?: return@execute
                    player.openShop(ui)
                }
            }
            execute<CommandSender> { sender, context, _ ->
                val player = onlinePlayers.find { it.name == sender.name } ?: return@execute
                val ui = plugin.shops.find { it.title == context["ui"] } ?: return@execute
                player.openShop(ui)
            }
        }

    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, context, _ ->
            plugin.settings.unload()
            plugin.settings.load()
        }
    }
}
