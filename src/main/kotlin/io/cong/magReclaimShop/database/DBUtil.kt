package io.cong.magReclaimShop.database

import io.cong.magReclaimShop.Settings
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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
}