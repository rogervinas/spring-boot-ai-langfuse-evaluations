package com.rogervinas.bank.tools

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import kotlin.collections.filter
import kotlin.jvm.java

@Configuration
class TransactionToolConfiguration {
    @Bean
    fun accountToolCallbackProvider(transactionTool: TransactionTool) = MethodToolCallbackProvider.builder()
        .toolObjects(transactionTool)
        .build()
}

@Service
class TransactionTool(private val transactionService: TransactionService) {
    @Tool(description = "Retrieve transactions from an account")
    fun getTransactions(
        @ToolParam(description = "The account identifier") accountId: String,
        @ToolParam(description = "The start date from which to retrieve transactions") dateFrom: LocalDate,
        @ToolParam(description = "The end date until which to retrieve transactions") dateTo: LocalDate,
    ) = transactionService.getTransactions(accountId, dateFrom, dateTo)
}

data class Transaction(
    val id: String,
    val date: LocalDate,
    val amount: Double,
    val merchant: String,
    val category: String
)

@Service
class TransactionService(private val transactionDatabase: TransactionDatabase) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    fun getTransactions(accountId: String, dateFrom: LocalDate, dateTo: LocalDate): List<Transaction> {
        logger.info("Get transactions for $accountId from $dateFrom to $dateTo")
        return transactionDatabase.findByAccountId(accountId)
            .filter { !it.date.isBefore(dateFrom) && !it.date.isAfter(dateTo) }
    }
}

@Service
class TransactionDatabase(clock: Clock) {

    private val today = LocalDate.now(clock)

    private val transactions = mapOf(
        "ACC-1001" to listOf(
            Transaction("TXN-ACC-1001-1", today.minusDays(20), -50.00, "Amazon", "Shopping"),
            Transaction("TXN-ACC-1001-2", today.minusDays(15), -120.00, "Hilton Hotels", "Travel"),
            Transaction("TXN-ACC-1001-3", today.minusDays(6), -9.99, "Netflix", "Entertainment"),
            Transaction("TXN-ACC-1001-4", today.minusDays(4), -35.50, "Whole Foods", "Groceries"),
            Transaction("TXN-ACC-1001-5", today.minusDays(2), 2500.00, "Employer Inc.", "Income"),
        ),
        "ACC-1002" to listOf(
            Transaction("TXN-ACC-1002-1", today.minusDays(28), -200.00, "Best Buy", "Electronics"),
            Transaction("TXN-ACC-1002-2", today.minusDays(21), -85.00, "Restaurant Le Fancy", "Dining"),
            Transaction("TXN-ACC-1002-3", today.minusDays(7), -15.99, "Spotify", "Entertainment"),
            Transaction("TXN-ACC-1002-4", today.minusDays(5), -450.00, "Delta Airlines", "Travel"),
            Transaction("TXN-ACC-1002-5", today.minusDays(1), -62.30, "Shell Gas Station", "Transport"),
        ),
        "ACC-1003" to listOf(
            Transaction("TXN-ACC-1003-1", today.minusDays(30), -1200.00, "Rent Payment", "Housing"),
            Transaction("TXN-ACC-1003-2", today.minusDays(22), -75.00, "Electric Company", "Utilities"),
            Transaction("TXN-ACC-1003-3", today.minusDays(17), -42.00, "Unknown Merchant XYZ", "Other"),
            Transaction("TXN-ACC-1003-4", today.minusDays(6), -1200.00, "Rent Payment", "Housing"),
            Transaction("TXN-ACC-1003-5", today.minusDays(3), 3200.00, "Freelance Client", "Income"),
        ),
    )

    fun findByAccountId(accountId: String): List<Transaction> =
        transactions.getOrDefault(accountId, emptyList())
}
