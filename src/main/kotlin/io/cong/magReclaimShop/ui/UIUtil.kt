package io.cong.magReclaimShop.ui

import io.cong.magReclaimShop.MagReclaimShop
import io.cong.magReclaimShop.Settings
import io.cong.magReclaimShop.database.SpecialItemsDB
import io.cong.magReclaimShop.database.SpecialItemsDB.specialItemsRecord
import io.cong.magReclaimShop.database.SpecialItemsDB.uuid
import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.types.Value
import io.cong.magReclaimShop.utils.TextUtil.format
import io.cong.magReclaimShop.utils.TextUtil.toLegacy
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.module.kether.KetherShell.eval
import taboolib.module.kether.ScriptOptions
import taboolib.module.nms.getItemTag
import taboolib.module.ui.conditionSlot
import taboolib.module.ui.lockSlots
import taboolib.module.ui.openMenu
import taboolib.module.ui.returnItems
import taboolib.module.ui.type.StorableChest
import taboolib.platform.util.buildItem
import java.util.concurrent.atomic.AtomicBoolean
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object UIUtil {
    fun Player.openShop(shop: Shop) {
        val str = transaction {
            val record = SpecialItemsDB
                .select {
                    (uuid eq uniqueId.toString()) and
                            (SpecialItemsDB.shop eq shop.title)
                }
                .singleOrNull()

            if (record != null) {
                record[specialItemsRecord]
            } else {
                val count = (shop.specialMin..shop.specialMax).random()
                val specialItems = shop.supportItems.shuffled().take(count)
                val serialized = specialItems.map { it.name }.toString()

                SpecialItemsDB.insert {
                    it[uuid] = uniqueId.toString()
                    it[SpecialItemsDB.shop] = shop.title
                    it[specialItemsRecord] = serialized
                }

                serialized
            }
        }

        val specialItems = str
            .removePrefix("[")
            .removeSuffix("]")
            .split(", ")
            .mapNotNull { MagReclaimShop.values.find { item -> item.name == it } }

        val openSlots = shop.buttons.filter { it.type == "putItem" }.flatMap {
            getOpenSlots(shop, it.key)
        }

        val sold = AtomicBoolean(false)

        val lockedSlots = shop.layout
            .joinToString("")
            .mapIndexedNotNull { index, c ->
                if (index !in openSlots) index else null
            }

        val normalValueMap = mutableMapOf<Int, Double>()
        val specialValueMap = mutableMapOf<Int, Double>()
        val slotValueMap = mutableMapOf<Int, Value>()

        openMenu<StorableChest>(title = shop.title) {
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

            rule {
                firstSlot { inventory, itemStack ->
                    openSlots.firstOrNull { slot ->
                        val item = inventory.getItem(slot)
                        item == null || item.type == Material.AIR
                    } ?: -1
                }

                writeItem { inventory, itemStack, slot, type ->
                    inventory.setItem(slot, itemStack.clone())

                    val items = openSlots.filter { inventory.getItem(it) != null }.map { it to inventory.getItem(it) }

                    specialValueMap.clear()
                    normalValueMap.clear()
                    slotValueMap.clear()

                    items.forEach { item ->
                        shop.supportItems.forEach { supportItem ->
                            val value = checkItemValue(item.second!!, supportItem)!!
                            val special = specialItems.contains(supportItem)

                            slotValueMap[item.first] = supportItem

                            val formula = if (special) {
                                supportItem.specialValueFormula
                            } else {
                                supportItem.normalValueFormula
                            }.replace("v", value)

                            val calValue = eval("calculate $formula").get().toString().toDouble()

                            if (special) {
                                specialValueMap.set(item.first, calValue * item.second!!.amount)
                            } else {
                                normalValueMap.set(item.first, calValue * item.second!!.amount)
                            }
                        }
                    }

                    shop.buttons.filter { it.type == "confirm" }.forEach { button ->
                        getSlots(button.key).forEach { slot ->
                            inventory.setItem(slot, buildItem(button.customMaterial) {
                                customModelData = button.customModelData
                                colored()
                            }.apply {
                                this.itemMeta.apply {
                                    displayName(format(button.customName))
                                    lore(button.customLore.flatMap {
                                        if (it == "%item-info%") {
                                            items.map { (slot, itemStack) ->
                                                    Settings.itemInfoFormat
                                                        .replace("%is-special%", if (specialValueMap.keys.contains(slot)) Settings.isSpecialFormat else Settings.notSpecialFormat)
                                                        .replace("%item-display-name%", slotValueMap[slot]?.displayItemName ?: "")
                                                        .replace("%special-display-name%", slotValueMap[slot]?.specialItemName ?: "")
                                                        .replace("%amount%", itemStack!!.clone().amount.toString())
                                                        .replace("%value-sum%", (if (specialValueMap.keys.contains(slot)) specialValueMap[slot] else normalValueMap[slot]).toString())
                                                }
                                        } else {
                                            listOf(
                                                it.replace("%normal-value-sum%", normalValueMap.values.sum().toString())
                                                .replace("%special-value-sum%", specialValueMap.values.sum().toString())
                                            )
                                        }
                                    }.map { format(it) })
                                    itemMeta = this
                                }
                            })
                        }
                    }
                }

                readItem { inventory, slot ->
                    inventory.getItem(slot)
                }
            }

            onClose { event ->
                if (!sold.get()) {
                    // 返还指定槽位的物品
                    val slots = openSlots
                    event.returnItems(slots)
//                    val player = event.player as Player
//                    player.sendMessage("§a物品已返还到你的背包")
                }
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
                        lore(button.customLore.map { format(
                            it.replace("%normal-value-sum%", "0")
                                .replace("%special-value-sum%", "0")
                                .replace("%item-info%", "请放入商品")
                        ) })
                        itemMeta = this
                    }
                }) {
                    button.action.forEach {
                        val normalSum = normalValueMap.values.sum()
                        val specialSum = specialValueMap.values.sum()
                        if (normalSum != 0.0 || specialSum != 0.0) {
                            console().performCommand(
                                it.replace("%normal-value-sum%", normalSum.toString())
                                    .replace("%special-value-sum%", specialSum.toString())
                            )

                            val valueAmountMap = mutableMapOf<Value, Int>()

                            openSlots.forEach value@{ slot ->
                                val item = inventory.getItem(slot) ?: return@value
                                if (item.type == Material.AIR) return@value

                                shop.supportItems.forEach { value ->
                                    val v = checkItemValue(item, value)
                                    if (v != null) {
                                        valueAmountMap[value] =
                                            (valueAmountMap[value] ?: 0) + item.amount
                                        return@value
                                    }
                                }
                            }

                            valueAmountMap.forEach {
                                for (i in 0..it.value) {
                                    val ketherRules = it.key.rules
                                    if (ketherRules.isNotEmpty()) {
                                        ketherRules.filter {
                                            eval(
                                                it.rule, options = ScriptOptions(
                                                    sender = adaptCommandSender(this.clicker)
                                                )
                                            ).get()
                                                .toString()
                                                .toBooleanStrictOrNull() == true
                                        }.filter {
                                            Math.random() < it.chance
                                        }.map {
                                            eval(
                                                it.action, options = ScriptOptions(
                                                    sender = adaptCommandSender(this.clicker)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            sold.set(true)
                            openShop(shop)
                        }
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

        val configValue = itemTag.get(item.configNBTID)?.asString()
        if (configValue != item.configNBTValue) {
            return null
        }

        return itemTag.get(item.valueNBTID)?.asString()
    }
}