package io.cong.magReclaimShop.command

import io.cong.magReclaimShop.MagReclaimShop
import io.cong.magReclaimShop.ui.UIUtil
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
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
                    UIUtil.openShop(player, ui)
                }
            }
            execute<CommandSender> { sender, context, _ ->
                val player = onlinePlayers.find { it.name == sender.name } ?: return@execute
                val ui = plugin.shops.find { it.title == context["ui"] } ?: return@execute
                UIUtil.openShop(player, ui)
            }
        }

    }
}
