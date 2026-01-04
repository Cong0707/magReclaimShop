package io.cong.magReclaimShop.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object TextUtil {
    private val mm = MiniMessage.builder()
        .preProcessor { input ->
            LegacyComponentSerializer
                .legacyAmpersand()
                .serialize(
                    LegacyComponentSerializer
                        .legacyAmpersand()
                        .deserialize(input)
                )
        }
        .build()

    fun format(input: String): Component {
        return mm.deserialize(input)
    }

    fun Component.toLegacy(): String {
        return LegacyComponentSerializer
            .legacyAmpersand()
            .serialize(this)
    }
}