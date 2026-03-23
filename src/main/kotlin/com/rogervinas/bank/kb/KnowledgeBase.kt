package com.rogervinas.bank.kb

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class KnowledgeBase(
    private val vectorStore: VectorStore,
    private val jdbcTemplate: JdbcTemplate,
    @Value("\${spring.ai.vectorstore.pgvector.table-name:vector_store}")
    private val tableName: String,
) {

    @PostConstruct
    fun init() {
        val logger = LoggerFactory.getLogger(KnowledgeBase::class.java)
        val vectorStoreCount = vectorStoreCount(jdbcTemplate)
        if (vectorStoreCount == null || vectorStoreCount == 0) {
            logger.info("Initializing KnowledgeBase ...")
            val documents = listOf(
                Document(
                    "Ref-DISP-001: Unauthorized transaction disputes must be submitted within 14 calendar days of the transaction date. Disputes submitted after this window will be automatically rejected.",
                    mapOf("category" to "dispute")
                ),
                Document(
                    "Ref-DISP-002: For merchant disputes (e.g., item not received), users must first attempt to contact the merchant. Proof of contact is required to initiate a formal chargeback through the app.",
                    mapOf("category" to "dispute")
                ),
                Document(
                    "Ref-DISP-003: Submitting a dispute for a transaction that you authorized is considered 'friendly fraud' and may lead to permanent account suspension.",
                    mapOf("category" to "dispute")
                ),
                Document(
                    "Ref-SEC-001: If a user reports a lost or stolen card, the 'Freeze Card' action must be offered immediately before any other troubleshooting steps.",
                    mapOf("category" to "security")
                ),
                Document(
                    "Ref-SEC-002: Spending limits can only be increased if the user has completed Level 2 Identity Verification (Biometric check).",
                    mapOf("category" to "security")
                ),
                Document(
                    "Ref-SEC-003: Cards can be temporarily frozen and un-frozen via the app. A permanent block requires a new card issuance fee of €5.00.",
                    mapOf("category" to "security")
                ),
                Document(
                    "Ref-FEE-001: Standard Tier users pay a 2% fee on all international ATM withdrawals. Metal Tier users have unlimited free international withdrawals.",
                    mapOf("category" to "fees")
                ),
                Document(
                    "Ref-FEE-002: Currency exchange is free on weekdays for all users. A 1% markup applies to exchanges made on weekends (UTC time).",
                    mapOf("category" to "fees")
                ),
            )
            vectorStore.add(documents)
            logger.info("KnowledgeBase initialized with ${documents.size} rules")
        } else {
            logger.info("KnowledgeBase already contains $vectorStoreCount rules")
        }
    }

    private fun vectorStoreCount(jdbcTemplate: JdbcTemplate) =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $tableName", Int::class.java)
}