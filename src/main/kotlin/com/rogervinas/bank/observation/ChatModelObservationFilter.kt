package com.rogervinas.bank.observation

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import org.springframework.ai.chat.observation.ChatModelObservationContext
import org.springframework.ai.model.tool.ToolCallingChatOptions
import org.springframework.ai.observation.ObservabilityHelper
import org.springframework.stereotype.Component

/**
 * ObservationFilter that adds prompt and completion content as key values
 * to ChatModel observation contexts using OTel semantic convention attributes.
 *
 * Replaces [org.springframework.ai.chat.observation.ChatModelPromptContentObservationHandler] and
 * [org.springframework.ai.chat.observation.ChatModelCompletionObservationHandler] which only log to SLF4J.
 */
@Component
class ChatModelObservationFilter : ObservationFilter {

    override fun map(context: Observation.Context): Observation.Context {
        if (context !is ChatModelObservationContext) return context

        fixResponseModel(context)

        val prompts = processPrompts(context)
        if (prompts.isNotEmpty()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.input.messages", ObservabilityHelper.concatenateStrings(prompts))
            )
        }

        val completions = processCompletions(context)
        if (completions.isNotEmpty()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.output.messages", ObservabilityHelper.concatenateStrings(completions))
            )
        }

        val toolDefinitions = processToolDefinitions(context)
        if (toolDefinitions.isNotEmpty()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.tool.definitions", ObservabilityHelper.concatenateStrings(toolDefinitions))
            )
        }

        return context
    }

    /**
     * Workaround for BedrockProxyChatModel not setting the model on ChatResponseMetadata.
     * Falls back to the request model so gen_ai.response.model is populated.
     */
    private fun fixResponseModel(context: ChatModelObservationContext) {
        val responseModel = context.response?.metadata?.model
        if (responseModel.isNullOrBlank()) {
            val requestModel = context.request.options?.model
            if (!requestModel.isNullOrBlank()) {
                context.addLowCardinalityKeyValue(KeyValue.of("gen_ai.response.model", requestModel))
            }
        }
    }

    private fun processPrompts(context: ChatModelObservationContext): List<String> {
        val instructions = context.request.instructions
        return if (instructions.isEmpty()) emptyList()
        else instructions.mapNotNull { it.text }
    }

    private fun processCompletions(context: ChatModelObservationContext): List<String> {
        val results = context.response?.results
        if (results.isNullOrEmpty()) return emptyList()
        return results
            .mapNotNull { it.output.text }
            .filter { it.isNotBlank() }
    }

    private fun processToolDefinitions(context: ChatModelObservationContext): List<String> {
        val options = context.request.options
        if (options !is ToolCallingChatOptions) return emptyList()
        val toolNames = buildSet {
            addAll(options.toolNames)
            addAll(options.toolCallbacks.map { it.toolDefinition.name() })
        }
        return toolNames.sorted()
    }
}
