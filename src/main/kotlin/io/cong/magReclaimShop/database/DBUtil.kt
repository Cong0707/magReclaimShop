package io.cong.magReclaimShop.database

import io.cong.magReclaimShop.Settings
import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.types.Value
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import taboolib.common.platform.function.getDataFolder
import java.sql.Connection
import java.sql.DriverManager

object DBUtil {
    private lateinit var db: Connection

    fun loadDatabase() {
        if (Settings.dbDebug) {
            Class.forName("org.h2.Driver")
            Database.connect({
                DriverManager.getConnection("jdbc:h2:${getDataFolder().absolutePath + "/database;MODE=MYSQL"}", "", "").also { db = it }//测试用
            })
            transaction {
                SchemaUtils.create(SpecialItemsDB)
            }
        } else {
            Class.forName("com.mysql.cj.jdbc.Driver")
            Database.connect({
                DriverManager.getConnection("jdbc:mysql://${Settings.host}:${Settings.port}/${Settings.database}?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8", Settings.user, Settings.password).also { db = it }
            })
            transaction {
                SchemaUtils.create(SpecialItemsDB)
            }
        }
    }

    fun unloadDatabase() {
        //do nothing
    }

    fun get(player: Player, shop: Shop): List<Value>? {
        return transaction {
            SpecialItemsDB
                .select {
                    (SpecialItemsDB.uuid eq player.uniqueId.toString()) and
                            (SpecialItemsDB.shop eq shop.id)
                }
                .firstOrNull()
                ?.get(SpecialItemsDB.specialItemsRecord)
        }?.let {
            deserialize(it) // 你已有的方法
        }
    }

    fun save(player: Player, shop: Shop, items: List<Value>) {
        val data = serialize(items) // 你已有的方法
        transaction {
            SpecialItemsDB.insert {
                it[uuid] = player.uniqueId.toString()
                it[this.shop] = shop.id
                it[specialItemsRecord] = data
            }
        }
    }

    fun delete(player: Player, shop: Shop) {
        transaction {
            SpecialItemsDB.deleteWhere {
                (uuid eq player.uniqueId.toString()) and
                        (this.shop eq shop.title)
            }
        }
    }
}