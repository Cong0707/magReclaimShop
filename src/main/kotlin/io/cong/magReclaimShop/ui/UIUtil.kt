package io.cong.magReclaimShop.ui

import io.cong.magReclaimShop.MagReclaimShop
import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.types.Value
import io.cong.magReclaimShop.utils.TextUtil.format
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.console
import taboolib.module.kether.KetherShell.eval
import taboolib.module.nms.getItemTag
import taboolib.module.ui.amountCondition
import taboolib.module.ui.openMenu
import taboolib.module.ui.returnItems
import taboolib.module.ui.type.StorableChest
import taboolib.platform.util.buildItem

object UIUtil {
    fun Player.openShop(shop: Shop) {
        val specialItems = listOf(shop.supportItems[0])

        val openSlots = shop.buttons.filter { it.type == "putItem" }.flatMap {
            getOpenSlots(shop, it.key)
        }

        openMenu<StorableChest>(title = shop.title) {
            rows(shop.layout.size)

            map(*shop.layout.toTypedArray())

            rule {
                openSlots.forEach { slot ->
                    checkSlot(slot) { inventory, itemStack ->
                        if (slot !in openSlots) return@checkSlot false
                        if (inventory.getItem(slot) != null) return@checkSlot true

                        shop.supportItems.forEach { supportItem ->
                            val value = checkItemValue(
                                itemStack,
                                supportItem
                            )
                            if (value != null) {
                                return@checkSlot true
                            }
                        }
                        false
                    }
                }


                firstSlot { inventory, itemStack ->
                    openSlots.firstOrNull { slot ->
                        val item = inventory.getItem(slot)
                        item == null || item.type == Material.AIR
                    } ?: -1
                }

                writeItem { inventory, itemStack, slot, _ ->
                    inventory.setItem(slot, itemStack.clone())
                }

                readItem { inventory, slot ->
                    inventory.getItem(slot)
                }
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