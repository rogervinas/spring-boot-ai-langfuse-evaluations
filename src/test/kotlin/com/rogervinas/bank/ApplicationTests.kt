package com.rogervinas.bank

import com.rogervinas.bank.chat.ChatService
import com.rogervinas.bank.evaluator.TestEvaluator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.evaluation.EvaluationRequest
import org.springframework.ai.evaluation.EvaluationResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.Wait.forLogMessage
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.util.UUID

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class ApplicationTests {

    companion object {
        @Container
        @JvmStatic
        val container = ComposeContainer(File("docker-compose-vectordb.yml"))
            .withExposedService("vectordb", 5432, forLogMessage(".*database system is ready to accept connections.*", 1))
    }

    @Autowired
    lateinit var chatClientBuilder: ChatClient.Builder

    @Autowired
    lateinit var chatService: ChatService

    @Test
    fun `should have tools available`() {
        val chatId = UUID.randomUUID().toString()
        val chatResponse = chatService.chat(
            chatId, "Standard", "ACC-1001",
            "Please enumerate the list of tools you have available"
        )

        val evaluationResult = evaluate(
            """
            The AI agent has at least these three tools available:
            get transactions, freeze card, and open dispute
            """.trimIndent(),
            chatResponse
        )

        assertThat(evaluationResult.isPass).isTrue.withFailMessage { evaluationResult.feedback }
    }

    @Test
    fun `should identify unauthorized netflix charge and offer freeze card and open dispute`() {
        val chatId = UUID.randomUUID().toString()
        val chatResponse = chatService.chat(
            chatId, "Standard", "ACC-1001",
            "I don't have Netflix but I see a charge on my account"
        )

        val evaluationResult = evaluate(
            """
            The AI agent found a Netflix charge of 9.99
            and offered the user to freeze the card and to open a dispute
            """.trimIndent(),
            chatResponse
        )

        assertThat(evaluationResult.isPass).isTrue.withFailMessage { evaluationResult.feedback }
    }

    @Test
    fun `should identify unauthorized electronics charge and offer only freeze card as dispute window expired`() {
        val chatId = UUID.randomUUID().toString()
        val chatResponse = chatService.chat(
            chatId, "Standard", "ACC-1002",
            "I see a Best Buy charge on my account but I never bought anything there"
        )

        val evaluationResult = evaluate(
            """
            The AI agent found a Best Buy charge of 200.00
            and offered the user to freeze the card
            but did NOT offer to open a dispute because the transaction is older than 14 days
            """.trimIndent(),
            chatResponse
        )

        assertThat(evaluationResult.isPass).isTrue.withFailMessage { evaluationResult.feedback }
    }

    private fun evaluate(claim: String, response: String): EvaluationResponse {
        return TestEvaluator(chatClientBuilder) { evaluationRequest, userSpec ->
            userSpec.text(
                """
                Your task is to evaluate if the answer given by an AI agent to a human user matches the claim.
                Return YES if the answer matches the claim and NO if it does not.
                After returning YES or NO, explain why.
                Answer: {answer}
                Claim: {claim}
            """.trimIndent()
            )
                .param("answer", evaluationRequest.responseContent)
                .param("claim", evaluationRequest.userText)
        }.evaluate(EvaluationRequest(claim, response))
    }
}
