package com.rogervinas.bank.chat

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatController(private val chatService: ChatService) {

    @PostMapping("/{userId}/chat")
    fun chat(
        @PathVariable userId: String,
        @RequestParam userTier: String,
        @RequestParam accountId: String,
        @RequestParam question: String
    ): String? {
        return chatService.chat(userId, userTier, accountId, question)
    }
}
