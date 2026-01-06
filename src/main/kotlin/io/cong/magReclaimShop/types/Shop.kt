package io.cong.magReclaimShop.types

data class Shop(
    val name: String,
    val title: String,
    val supportItems: List<Value>,
    val specialMin: Int,
    val specialMax: Int,
    val layout: List<String>,
    val buttons: List<ButtonConfig>
)
