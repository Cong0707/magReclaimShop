package io.cong.magReclaimShop

import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.types.Value
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import java.io.File

object MagReclaimShop : Plugin() {
    var values = mutableListOf<Value>()
    var shops = mutableListOf<Shop>()

    lateinit var plugin: MagReclaimShop
    lateinit var settings: Settings

    override fun onLoad() {
        // Plugin startup logic
        plugin = this
        settings = Settings(plugin)
        tryLoadConfig()
    }

    override fun onEnable() {
        settings.load()
    }

    override fun onDisable() {
        settings.unload()
    }

    fun tryLoadConfig() {
        val config =  File(getDataFolder(), "config.yml")
        val valuesDir = File(getDataFolder(), "values")
        val shopsDir = File(getDataFolder(), "shops")

        if (!config.exists()) {
            releaseResourceFile("config.yml")
        }

        if (!valuesDir.exists()) {
            valuesDir.mkdirs()
            releaseResourceFile("values/value.yml")
        }

        if (!shopsDir.exists()) {
            shopsDir.mkdirs()
            releaseResourceFile("shops/shop.yml")
        }
    }
}
