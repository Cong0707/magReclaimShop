package io.cong.magReclaimShop.database

import io.cong.magReclaimShop.Settings
import org.jetbrains.exposed.dao.id.IntIdTable

object SpecialItemsDB: IntIdTable(Settings.prefix + "collectibles") {
    val uuid = varchar("uuid", 64)
    val shop = varchar("shop", 64)
    val specialItems = text("special_items")
}