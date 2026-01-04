package io.cong.magReclaimShop.types

import org.bukkit.Material

data class Value(
    val name: String,
    val configNBTID: String,
    val configNBTValue: String,
    val valueNBTID: String,
    val normalValueFormula: String,
    val specialValueFormula: String,
    val specialItemMaterial: Material,
    val specialItemCustomModel: Int,
    val specialItemName: String,
    val specialItemLore: List<String>,
    val triggerCommandOnSell: Boolean,
    val rules: List<KetherRule>
)