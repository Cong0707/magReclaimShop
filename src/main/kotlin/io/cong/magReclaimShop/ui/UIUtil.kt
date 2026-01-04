package io.cong.magReclaimShop.ui

import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.types.Value
import io.cong.magReclaimShop.utils.TextUtil.format
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.console
import taboolib.module.nms.getItemTag
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.lockSlots
import taboolib.module.ui.openMenu
import taboolib.module.ui.returnItems
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem

object UIUtil {
    fun Player.openShop(shop: Shop) {
        val specialItems = listOf(shop.supportItems[0])

        val openSlots = shop.buttons.filter { it.type == "putItem" }.flatMap {
            getOpenSlots(shop, it.key)
        }

        val lockedSlots = shop.layout
            .joinToString("")
            .mapIndexedNotNull { index, c ->
                if (index !in openSlots) index else null
            }

        openMenu<Chest>(title = shop.title) {
            rows(shop.layout.size)

            map(*shop.layout.toTypedArray())

            onClick(lock = false) { event ->
                openSlots.forEach { slot ->
                    event.conditionSlot(slot,
                        condition = { put, out ->
                            if (put != null) {
                                if (put.type == Material.AIR) return@conditionSlot true
                                shop.supportItems.forEach { supportItem ->
                                    val value = checkItemValue(put, supportItem)
                                    if (value != null) {
                                        return@conditionSlot true
                                    }
                                }
                                false
                            } else {
                                true  // 允许取出
                            }
                        }
                    )
                }

                event.lockSlots(lockedSlots)
            }

            onClose { event ->
                // 返还指定槽位的物品
                val slots = openSlots
                event.returnItems(slots)

                val player = event.player as Player
                player.sendMessage("§a物品已返还到你的背包")
            }

            shop.buttons.find { it.type == "specialItem" }?.let { pageButtons ->
                val slots = getSlots(pageButtons.key)

                slots.zip(specialItems).forEach {
                    val item = it.second
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
//                    val special = specialItems.contains(supportItem)
//
//                    val formula = if (special) {
//                        supportItem.specialValueFormula
//                    } else {
//                        supportItem.normalValueFormula
//                    }.replace("v", value)
//
//                    val calValue = eval("calculate $formula").get().toString().toDouble()
//
//                    sendMessage("价值$calValue * ${itemStack.amount}")
//
//                    if (special) {
//                        specialValueMap.set(slot, calValue * itemStack.amount)
//                    } else {
//                        normalValueMap.set(slot, calValue * itemStack.amount)
//                    }
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
        }
    }

    fun getOpenSlots(shop: Shop, key: Char): List<Int> {
        return shop.layout
            .joinToString("")
            .mapIndexedNotNull { index, c ->
                if (c == key) index else null
            }
    }

    fun checkItemValue(
        itemStack: ItemStack,
        item: Value
    ): String? {
        val itemTag = try {
            itemStack.getItemTag()
        } catch (e: Exception) {
            return null
        }

        // 读取 configNBT
        val configValue = itemTag.getDeep(item.configNBTID)?.asString()
        if (configValue != item.configNBTValue) {
            return null
        }

        // 获取 valueNBT
        return itemTag.getDeep(item.valueNBTID)?.asString()
    }
}