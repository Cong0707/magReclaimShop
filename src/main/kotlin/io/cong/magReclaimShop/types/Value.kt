package io.cong.magReclaimShop.types

data class Value(
    val configNBTID: String,
    val configNBTValue: String,
    val valueNBTID: String,
    val normalValueFormula: String,
    val specialValueFormula: String,
    val specialItemID: String,
    val specialItemCustomModel: String,
    val specialItemName: String,
    val specialItemLore: List<String>,
    val triggerCommandOnSell: Boolean,
    val rules: List<KetherRule>
)