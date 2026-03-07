package com.rogervinas.bank.tools

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@Configuration
class CardToolConfiguration {
    @Bean
    fun cardToolCallbackProvider(cardTool: CardTool) = MethodToolCallbackProvider.builder()
        .toolObjects(cardTool)
        .build()
}

@Service
class CardTool(private val cardService: CardService) {
    @Tool(description = "Freeze the card associated with an account")
    fun freezeCard(
        @ToolParam(description = "The account identifier") accountId: String,
        @ToolParam(description = "The reason for freezing the card") reason: String,
    ) = cardService.freezeCard(accountId, reason)

    @Tool(description = "Unfreeze the card associated with an account")
    fun unfreezeCard(
        @ToolParam(description = "The account identifier") accountId: String,
    ) = cardService.unfreezeCard(accountId)

    @Tool(description = "Check if the card associated with an account is frozen")
    fun isCardFrozen(
        @ToolParam(description = "The account identifier") accountId: String,
    ) = cardService.isCardFrozen(accountId)
}

@Service
class CardService {
    private val logger = LoggerFactory.getLogger(CardService::class.java)
    private val frozenCards = mutableSetOf<String>()

    fun freezeCard(accountId: String, reason: String) {
        logger.info("Freezing card for $accountId because $reason")
        frozenCards.add(accountId)
    }

    fun unfreezeCard(accountId: String) {
        logger.info("Unfreezing card for $accountId")
        frozenCards.remove(accountId)
    }

    fun isCardFrozen(accountId: String): Boolean {
        logger.info("Checking if card for $accountId is frozen")
        return accountId in frozenCards
    }
}
