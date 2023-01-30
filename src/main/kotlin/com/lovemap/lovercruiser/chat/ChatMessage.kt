package com.lovemap.lovercruiser.chat

import java.time.Instant

data class ChatMessage(
    val jwt: String,
    val chatId: Long,
    val senderLoverId: Long,
    val content: String,
    val sentAt: Instant,
)