package com.rogervinas.bank.chat

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatService(
    vectorStore: VectorStore,
    private val clock: Clock,
    private val chatClient: ChatClient
) {

    private val questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore).build()
    private val simpleLoggerAdvisor = SimpleLoggerAdvisor()
    private val chatMemory = ConcurrentHashMap<String, PromptChatMemoryAdvisor>()

    fun chat(
        chatId: String,
        userTier: String,
        accountId: String,
        question: String
    ): ChatResponse {
        val chatMemoryAdvisor = chatMemory.computeIfAbsent(chatId) {
            PromptChatMemoryAdvisor.builder(
                MessageWindowChatMemory.builder()
                    .chatMemoryRepository(InMemoryChatMemoryRepository())
                    .build()
            ).build()
        }
        return chatClient
            .prompt()
            .system {
                it.params(mapOf(
                    "userTier" to userTier,
                    "currentDate" to LocalDate.now(clock),
                    "accountId" to accountId
                ))
            }
            .user(question)
            .advisors(questionAnswerAdvisor, chatMemoryAdvisor, simpleLoggerAdvisor)
            .call()
            .entity(ChatResponse::class.java)!!
    }
}
