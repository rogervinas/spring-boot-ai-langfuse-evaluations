package com.rogervinas.bank.chat

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.UUID

@WebMvcTest(ChatController::class)
@AutoConfigureWebTestClient
@Import(ChatControllerTest.Config::class)
class ChatControllerTest {

    @TestConfiguration
    class Config {
        @Bean
        fun observationRegistry(): ObservationRegistry = ObservationRegistry.NOOP
    }

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockitoBean
    lateinit var chatService: ChatService

    @Test
    fun `should serve chat requests`() {
        val userId = UUID.randomUUID().toString()
        val userTier = "Standard"
        val accountId = "ACC-1001"
        val chatQuestion = "What can you help me with?"
        val chatResponse = ChatResponse(
            answer = "I can help you with transactions, cards, and disputes",
            suggestedActions = listOf(SuggestedAction.GET_TRANSACTIONS, SuggestedAction.FREEZE_CARD)
        )

        doReturn(chatResponse)
            .whenever(chatService)
            .chat(userId, userTier, accountId, chatQuestion)

        webTestClient.post()
            .uri("/$userId/chat")
            .contentType(APPLICATION_FORM_URLENCODED)
            .bodyValue("userTier=$userTier&accountId=$accountId&question=$chatQuestion")
            .exchange()
            .expectStatus().isOk
            .expectBody<ChatResponse>()
            .consumeWith { response ->
                assertThat(response.responseBody).isEqualTo(chatResponse)
            }
    }
}
