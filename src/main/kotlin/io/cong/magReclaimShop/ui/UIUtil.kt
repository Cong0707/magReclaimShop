package io.cong.magReclaimShop.ui

import io.cong.magReclaimShop.types.Shop
import org.bukkit.entity.Player
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.amountCondition
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem

object UIUtil {
    fun Player.openShop(shop: Shop) {
        openMenu<Chest>(title = "数量限制菜单") {
            rows(3)

            map(
                "#########",
                "# A   B #",
                "#########"
            )

            set('#', XMaterial.GRAY_STAINED_GLASS_PANE) { name = " " }

            set('A', buildItem(XMaterial.CHEST) {
                name = "§e限制 10 个"
                lore += "§7最多只能放 10 个物品"
            })

            set('B', buildItem(XMaterial.BARREL) {
                name = "§e限制 1 个"
                lore += "§7最多只能放 1 个物品"
            })

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