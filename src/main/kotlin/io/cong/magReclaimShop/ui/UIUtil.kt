package io.cong.magReclaimShop.ui

import io.cong.magReclaimShop.MagReclaimShop
import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.utils.TextUtil.format
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.module.ui.amountCondition
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem

object UIUtil {
    fun Player.openShop(shop: Shop) {
        openMenu<Chest>(title = "数量限制菜单") {
            rows(shop.layout.size)

            map(*shop.layout.toTypedArray())

            shop.buttons.find { it.type == "show" }?.let { pageButtons ->
                val slots = getSlots(pageButtons.key)
                val items = listOf(MagReclaimShop.values[1])

                slots.zip(items).forEach {
                    val item = it.second ?: return@forEach
                    set(it.first, buildItem(item.specialItemMaterial) {
                        customModelData = item.specialItemCustomModel
                        colored()
                    }.apply {
                        this.itemMeta.apply {
                            displayName(format(item.specialItemName))
                            lore(item.specialItemLore.map { format(it) })
                            itemMeta = this
                        }
                    })
                }
            }

            shop.buttons.filter { it.type == "confirm" }.forEach { button ->
                set(button.key, buildItem(button.customMaterial) {
                    customModelData = button.customModelData
                    colored()
                }.apply {
                    this.itemMeta.apply {
                        displayName(format(button.customName))
                        lore(button.customLore.map { format(it) })
                        itemMeta = this
                    }
                }) {
                    button.action.forEach {
                        console().performCommand(it)
                    }
                }
            }

            shop.buttons.filter { it.type == "decoration" }.forEach { button ->
                set(button.key, buildItem(button.customMaterial) {
                    customModelData = button.customModelData
                    colored()
                }.apply {
                    this.itemMeta.apply {
                        displayName(format(button.customName))
                        lore(button.customLore.map { format(it) })
                        itemMeta = this
                    }
                })
            }

            onClick(lock = false) { event ->
                val slotA = getFirstSlot('A')
                val slotB = getFirstSlot('B')

                // A 槽位：最多 10 个
                event.amountCondition(slotA, amount = 10) {
                    event.clicker.sendMessage("§c该槽位最多只能放 10 个物品！")
                }

                // B 槽位：最多 1 个（单个物品）
                event.amountCondition(slotB, amount = 1) {
                    event.clicker.sendMessage("§c该槽位只能放 1 个物品！")
                }
            }
        }
    }
}