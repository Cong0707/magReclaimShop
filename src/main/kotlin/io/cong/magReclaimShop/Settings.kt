package io.cong.magReclaimShop

import io.cong.magReclaimShop.types.ButtonConfig
import io.cong.magReclaimShop.types.KetherRule
import io.cong.magReclaimShop.types.Shop
import io.cong.magReclaimShop.types.Value
import org.bukkit.Material
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import java.io.File

class Settings(val plugin: MagReclaimShop) {
    companion object {
        lateinit var itemInfoFormat: String
        lateinit var isSpecialFormat: String
        lateinit var notSpecialFormat: String
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
        isSpecialFormat = config.getString("is-special-format") ?: "yes"
        notSpecialFormat = config.getString("not-special-format") ?: "no"

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
        val rulesSection = configuration.getMapList("rules")

        return Value(
            name = configuration.getString("name") ?: "",
            configNBTID = configuration.getString("config-nbt-id") ?: "",
            configNBTValue = configuration.getString("config-nbt-value") ?: "",
            valueNBTID = configuration.getString("value-nbt-id") ?: "",
            normalValueFormula = configuration.getString("normal-value-formula") ?: "",
            specialValueFormula = configuration.getString("special-value-formula") ?: "",
            specialItemMaterial = Material.matchMaterial(configuration.getString("special-item-material") ?: "STONE") ?: Material.STONE,
            specialItemCustomModel = configuration.getInt("special-item-custom-model") ?: 0,
            specialItemName = configuration.getString("special-item-name") ?: "",
            specialItemLore = configuration.getStringList("special-item-lore"),
            triggerCommandOnSell = configuration.getBoolean("trigger-command-on-sell"),
            rules = parseKetherRules(rulesSection)
        )
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
        val supportItems = configuration.getStringList("support-items")
            .mapNotNull { id -> plugin.values.find { it.name == id } }

        val buttons = configuration.getMapList("buttons").mapNotNull { map ->
            val keyStr = map["key"] as? String ?: return@mapNotNull null
            val type = map["type"] as? String ?: return@mapNotNull null

            ButtonConfig(
                key = keyStr.first(),
                type = type,
                customMaterial = Material.matchMaterial(map["custom-material"] as? String ?: "STONE") ?: Material.STONE,
                customName = map["custom-name"] as? String ?: "",
                customModelData = (map["custom-model-data"] as? String)?.toIntOrNull() ?: 0,
                customLore = map["custom-lore"] as? List<String> ?: emptyList(),
                action = map["action"] as? List<String> ?: emptyList()
            )
        }

        return Shop(
            title = configuration.getString("title") ?: "shop",
            supportItems = supportItems,
            specialMin = configuration.getInt("special-min"),
            specialMax = configuration.getInt("special-max"),
            layout = configuration.getStringList("layout"),
            buttons = buttons
        )
    }

    // 解析规则列表
    private fun parseKetherRules(section: List<Map<*, *>>): List<KetherRule> {
        return section.mapNotNull { ruleMap ->
            val rule = (ruleMap["rule"] as? String ?: "")
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ") // 压缩多余空格
            val action = (ruleMap["action"] as? String ?: "")
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ") // 压缩多余空格
            if (rule.isEmpty() && action.isEmpty()) {
                null // 完全没有内容的条目就忽略
            } else {
                val chance = (ruleMap["chance"] as? String)?.removeSuffix("%")?.toFloat()?.div(100)
                    ?: 1f
                KetherRule(rule, chance, action)
            }
        }
    }
}