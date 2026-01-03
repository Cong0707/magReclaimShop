package io.cong.magReclaimShop.types

data class KetherRule(
    val rule: String,       // 规则脚本
    val chance: Float = 1f, // 执行概率，可选
    val action: String      // 动作脚本
)