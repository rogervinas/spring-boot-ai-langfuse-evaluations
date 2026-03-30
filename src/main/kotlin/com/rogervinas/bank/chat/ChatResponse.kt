package com.rogervinas.bank.chat

data class ChatResponse(
    val answer: String,
    val suggestedActions: List<SuggestedAction>,
)

enum class SuggestedAction {
    FREEZE_CARD,
    UNFREEZE_CARD,
    OPEN_DISPUTE,
    CHECK_DISPUTE_STATUS,
    GET_TRANSACTIONS,
}