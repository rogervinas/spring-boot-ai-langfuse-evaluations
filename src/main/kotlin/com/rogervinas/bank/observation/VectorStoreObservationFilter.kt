package com.rogervinas.bank.observation

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationFilter
import org.springframework.ai.observation.ObservabilityHelper
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext
import org.springframework.stereotype.Component

/**
 * ObservationFilter that adds query response documents as key values
 * to VectorStore observation contexts using OTel semantic convention attributes.
 *
 * Replaces [org.springframework.ai.vectorstore.observation.VectorStoreQueryResponseObservationHandler] which only logs to SLF4J.
 */
@Component
class VectorStoreObservationFilter : ObservationFilter {

    override fun map(context: Observation.Context): Observation.Context {
        if (context !is VectorStoreObservationContext) return context

        val documents = context.queryResponse
        if (!documents.isNullOrEmpty()) {
            val texts = documents.mapNotNull { it.text }
            if (texts.isNotEmpty()) {
                context.addHighCardinalityKeyValue(
                    KeyValue.of("gen_ai.retrieval.documents", ObservabilityHelper.concatenateStrings(texts))
                )
            }
        }

        return context
    }
}
