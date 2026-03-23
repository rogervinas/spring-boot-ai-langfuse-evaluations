package com.rogervinas.bank.chat

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatController(
    private val chatService: ChatService,
    private val observationRegistry: ObservationRegistry
) {

    @PostMapping("/{userId}/chat")
    fun chat(
        @PathVariable userId: String,
        @RequestParam userTier: String,
        @RequestParam accountId: String,
        @RequestParam question: String
    ): String? {
        val observation = Observation.createNotStarted("chat", observationRegistry)
            .highCardinalityKeyValue("langfuse.user.id", userId)

        return observation.observe<String> {
            chatService.chat(userId, userTier, accountId, question)
        }
    }
}
