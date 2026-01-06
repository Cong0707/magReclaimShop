package io.cong.magReclaimShop.command

import io.cong.magReclaimShop.MagReclaimShop
import io.cong.magReclaimShop.database.SpecialItemsDB
import io.cong.magReclaimShop.ui.UIUtil.openShop
import org.bukkit.command.CommandSender
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
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
                plugin.shops.map { it.name }
            }
            dynamic("player") {
                suggestPlayers()
                execute<CommandSender> { sender, context, _ ->
                    val player = onlinePlayers.find { it.name == context["player"] } ?: return@execute
                    val ui = plugin.shops.find { it.name == context["ui"] } ?: return@execute
                    player.openShop(ui)
                }
            }
            execute<CommandSender> { sender, context, _ ->
                val player = onlinePlayers.find { it.name == sender.name } ?: return@execute
                val ui = plugin.shops.find { it.name == context["ui"] } ?: return@execute
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

    @CommandBody
    val clear = subCommand {

        // /magrs clear <player>
        dynamic("player") {
            suggestPlayers()

            execute<CommandSender> { sender, context, _ ->
                val player = onlinePlayers.find { it.name == context["player"] } ?: return@execute

                transaction {
                    SpecialItemsDB.deleteWhere {
                        uuid eq player.uniqueId.toString()
                    }
                }

                sender.sendMessage("§a已清空玩家 ${player.name} 的全部商店热卖商品记录")
            }

            // /magrs clear <player> <shop>
            dynamic("shop") {
                suggestion<CommandSender> { _, _ ->
                    plugin.shops.map { it.name }
                }

                execute<CommandSender> { sender, context, _ ->
                    val player = onlinePlayers.find { it.name == context["player"] } ?: return@execute
                    val shop = plugin.shops.find { it.name == context["shop"] } ?: return@execute

                    transaction {
                        SpecialItemsDB.deleteWhere {
                            (uuid eq player.uniqueId.toString()) and
                                    (SpecialItemsDB.shop eq shop.name)
                        }
                    }

                    sender.sendMessage("§a已清空玩家 ${player.name} 的商店 ${shop.name} 热卖商品记录")
                }
            }
        }
    }

}
