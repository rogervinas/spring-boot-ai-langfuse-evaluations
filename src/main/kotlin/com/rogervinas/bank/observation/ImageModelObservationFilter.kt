package com.rogervinas.bank.observation

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import org.springframework.ai.image.observation.ImageModelObservationContext
import org.springframework.ai.observation.ObservabilityHelper
import org.springframework.stereotype.Component

/**
 * ObservationFilter that adds prompt content as key values
 * to ImageModel observation contexts using OTel semantic convention attributes.
 *
 * Replaces [org.springframework.ai.image.observation.ImageModelPromptContentObservationHandler] which only logs to SLF4J.
 */
@Component
class ImageModelObservationFilter : ObservationFilter {

    override fun map(context: Observation.Context): Observation.Context {
        if (context !is ImageModelObservationContext) return context

        val instructions = context.request.instructions
        if (instructions.isNotEmpty()) {
            val prompts = instructions.mapNotNull { it.text }
            if (prompts.isNotEmpty()) {
                context.addHighCardinalityKeyValue(
                    KeyValue.of("gen_ai.input.messages", ObservabilityHelper.concatenateStrings(prompts))
                )
            }
        }

        return context
    }
}
