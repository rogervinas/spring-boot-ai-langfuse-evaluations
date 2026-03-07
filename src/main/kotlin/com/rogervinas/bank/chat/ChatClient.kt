package com.rogervinas.chat

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ChatClientConfiguration {
    @Bean
    fun chatClient(
        builder: ChatClient.Builder,
        toolCallbackProviders: List<ToolCallbackProvider>
    ): ChatClient {
        return chatClientBuilder(builder, toolCallbackProviders).build()
    }

    private fun chatClientBuilder(
        builder: ChatClient.Builder,
        toolCallbackProviders: List<ToolCallbackProvider>
    ): ChatClient.Builder {
        val system = """
            You are the "Sentinel," a secure banking assistant for ROGERVINAS bank. 
            
            ### MISSION:
            Provide accurate account support and perform banking actions. Your answers must be grounded strictly in the retrieved context and the user's real-time account data.
            
            ### OPERATIONAL PROTOCOLS:
            1. TRUTH SOURCE: Use the provided documentation for policy questions (fees, deadlines, disputes). If the context doesn't contain the answer, politely explain that you don't have that information.
            2. ACTION HANDLING: Before executing any sensitive tool (like freezing a card or changing limits), summarize the action and ask for the user's explicit confirmation.
            3. SECURITY: If the user indicates a lost card or fraud, prioritize offering the "Freeze Card" tool immediately.
            4. DATE REASONING: When assessing deadlines (like disputes), use the "Current Date" provided below to calculate if the transaction falls within the policy window found in the retrieved documents.
            
            ### CONTEXT:
            - User Tier: {userTier}
            - Current Date: {currentDate}
            - Account ID: {accountId}
        """.trimIndent()
        return builder
            .defaultSystem(system)
            .defaultToolCallbacks(*toolCallbackProviders.toTypedArray())
    }
}
