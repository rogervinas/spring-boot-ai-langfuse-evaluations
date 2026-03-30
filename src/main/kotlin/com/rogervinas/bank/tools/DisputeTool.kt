package com.rogervinas.bank.tools

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@Configuration
class DisputeToolConfiguration {
    @Bean
    fun disputeToolCallbackProvider(disputeTool: DisputeTool) = MethodToolCallbackProvider.builder()
        .toolObjects(disputeTool)
        .build()
}

@Service
class DisputeTool(private val disputeService: DisputeService) {
    @Tool(description = "Open a dispute for a transaction")
    fun openDispute(
        @ToolParam(description = "The account identifier") accountId: String,
        @ToolParam(description = "The transaction identifier") transactionId: String,
        @ToolParam(description = "The reason for the dispute") reason: String,
    ) = disputeService.openDispute(accountId, transactionId, reason)

    @Tool(description = "Check the status of a dispute")
    fun getDisputeStatus(
        @ToolParam(description = "The dispute identifier") disputeId: String,
    ) = disputeService.getDisputeStatus(disputeId)

    @Tool(description = "List all dispute ids for an account")
    fun listDisputes(
        @ToolParam(description = "The account identifier") accountId: String,
    ) = disputeService.listDisputes(accountId)
}

data class Dispute(
    val id: String,
    val accountId: String,
    val transactionId: String,
    val amount: Double,
    val reason: String,
    val status: String,
)

@Service
class DisputeService(private val transactionDatabase: TransactionDatabase) {
    private val logger = LoggerFactory.getLogger(DisputeService::class.java)
    private val disputes = mutableMapOf<String, Dispute>()
    private var nextId = 1

    fun openDispute(accountId: String, transactionId: String, reason: String): Dispute {
        val disputeId = "DSP-$accountId-${nextId++}"
        val amount = transactionDatabase.findByAccountId(accountId)
            .find { it.id == transactionId }?.amount ?: 0.0
        logger.info("Opening dispute $disputeId for transaction $transactionId (amount=$amount) on account $accountId: $reason")
        val dispute = Dispute(disputeId, accountId, transactionId, amount, reason, "OPEN")
        disputes[disputeId] = dispute
        return dispute
    }

    fun listDisputes(accountId: String): List<String> {
        logger.info("Listing disputes for account $accountId")
        return disputes.values.filter { it.accountId == accountId }.map { it.id }
    }

    fun getDisputeStatus(disputeId: String): Dispute? {
        logger.info("Checking status of dispute $disputeId")
        val dispute = disputes[disputeId] ?: return null
        if (isOpen(dispute)) {
            val pending = markAsPending(dispute)
            disputes[disputeId] = pending
            return pending
        }
        if (isPending(dispute)) {
            val resolved = resolve(dispute)
            disputes[disputeId] = resolved
            return resolved
        }
        return dispute
    }

    private fun isOpen(dispute: Dispute): Boolean = dispute.status == "OPEN"

    private fun isPending(dispute: Dispute): Boolean = dispute.status == "PENDING"

    private fun markAsPending(dispute: Dispute): Dispute = dispute.copy(status = "PENDING")

    // POC: reject disputes for amounts greater than 10
    private fun resolve(dispute: Dispute): Dispute {
        val status = if (kotlin.math.abs(dispute.amount) > 10) "REJECTED" else "ACCEPTED"
        return dispute.copy(status = status)
    }
}