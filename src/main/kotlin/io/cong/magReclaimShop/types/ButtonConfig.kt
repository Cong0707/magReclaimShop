package io.cong.magReclaimShop.types

import org.bukkit.Material

data class ButtonConfig(
    val key: Char,
    val customMaterial: Material,
    val customName: String,
    val customModelData: Int,
    val action: List<String>,
    val customLore: List<String>,
    val type: String
)

