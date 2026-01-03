package io.cong.magReclaimShop

import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.types.Value
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import java.io.File

class Settings(val plugin: MagReclaimShop) {
    companion object {
        lateinit var itemInfoFormat: String
    }

    fun load() {
        loadConfig()
        loadValues()
        loadShops()
    }

    fun unload() {
        plugin.shops.clear()
        plugin.values.clear()
    }

    fun loadConfig() {
        val configYml =  File(getDataFolder(), "config.yml")
        val config = Configuration.loadFromFile(configYml)

        itemInfoFormat = config.getString("item-info-format") ?: ""
    }

    fun loadValues() {
        val folder = File(getDataFolder(), "values")
        if (!folder.exists()) folder.mkdirs()

        folder.listFiles { file -> file.isFile && file.extension == "yml" }?.forEach { file ->
            val config = Configuration.loadFromFile(file)

            config.getKeys(false).forEach yml@{ groupId ->
                val configurationSection = config.getConfigurationSection(groupId) ?: return@yml
                plugin.values.add(loadValuesSettings(configurationSection))
            }
        }
    }

    fun loadValuesSettings(configuration: ConfigurationSection): Value {

    }

    fun loadShops() {
        val folder = File(getDataFolder(), "shops")
        if (!folder.exists()) folder.mkdirs()

        folder.listFiles { file -> file.isFile && file.extension == "yml" }?.forEach { file ->
            val config = Configuration.loadFromFile(file)

            config.getKeys(false).forEach yml@{ groupId ->
                val configurationSection = config.getConfigurationSection(groupId) ?: return@yml
                plugin.shops.add(loadShopsSettings(configurationSection))
            }
        }
    }

    fun loadShopsSettings(configuration: ConfigurationSection): Shop {

    }
}