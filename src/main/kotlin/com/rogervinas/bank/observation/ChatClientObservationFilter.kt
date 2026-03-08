package com.rogervinas.bank.observation

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import org.springframework.ai.chat.client.observation.ChatClientObservationContext
import org.springframework.ai.observation.ObservabilityHelper
import org.springframework.stereotype.Component

/**
 * ObservationFilter that adds prompt and completion content as key values
 * to ChatClient observation contexts using OTel semantic convention attributes.
 *
 * Replaces [org.springframework.ai.chat.client.observation.ChatClientPromptContentObservationHandler] and
 * [org.springframework.ai.chat.client.observation.ChatClientCompletionObservationHandler] which only log to SLF4J.
 */
@Component
class ChatClientObservationFilter : ObservationFilter {

    override fun map(context: Observation.Context): Observation.Context {
        if (context !is ChatClientObservationContext) return context

        val prompts = processPrompts(context)
        if (prompts.isNotEmpty()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.input.messages", ObservabilityHelper.concatenateEntries(prompts))
            )
        }

        val completions = processCompletions(context)
        if (completions.isNotEmpty()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.output.messages", ObservabilityHelper.concatenateStrings(completions))
            )
        }

        return context
    }

    private fun processPrompts(context: ChatClientObservationContext): Map<String, Any> {
        val instructions = context.request.prompt().instructions
        if (instructions.isEmpty()) return emptyMap()
        val messages = mutableMapOf<String, Any>()
        instructions.forEach { message ->
            messages[message.messageType.value] = message.text ?: ""
        }
        return messages
    }

    private fun processCompletions(context: ChatClientObservationContext): List<String> {
        val chatResponse = context.response?.chatResponse() ?: return emptyList()
        return chatResponse.results
            .mapNotNull { it.output }
            .mapNotNull { it.text }
            .filter { it.isNotBlank() }
    }
}
