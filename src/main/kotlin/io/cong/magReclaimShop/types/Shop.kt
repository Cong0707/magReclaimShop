package io.cong.magReclaimShop.types

data class Shop(
    val supportItems: List<Value>,
    val specialMin: Int,
    val specialMax: Int,
    val craftingLayout: List<String>,
    val craftingButtons: List<ButtonConfig>
)
