package com.rogervinas.bank.observation

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import org.springframework.ai.tool.observation.ToolCallingObservationContext
import org.springframework.stereotype.Component

/**
 * ObservationFilter that adds tool call arguments and results as key values
 * to ToolCalling observation contexts using OTel semantic convention attributes.
 *
 * Replaces [org.springframework.ai.tool.observation.ToolCallingContentObservationFilter] which uses
 * spring.ai.tool.call.* keys and is gated behind a @ConditionalOnProperty
 * that may not be enabled.
 */
@Component
class ToolCallingObservationFilter : ObservationFilter {

    override fun map(context: Observation.Context): Observation.Context {
        if (context !is ToolCallingObservationContext) return context

        val toolName = context.toolDefinition.name()
        if (toolName.isNotBlank()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.tool.name", toolName)
            )
        }

        val toolDescription = context.toolDefinition.description()
        if (!toolDescription.isNullOrBlank()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.tool.description", toolDescription)
            )
        }

        val arguments = context.toolCallArguments
        if (arguments.isNotEmpty()) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.tool.call.arguments", arguments)
            )
        }

        val result = context.toolCallResult
        if (result != null) {
            context.addHighCardinalityKeyValue(
                KeyValue.of("gen_ai.tool.call.result", result)
            )
        }

        return context
    }
}
