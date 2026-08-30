package io.vopenia.livekit.participant.chat

/**
 * LiveKit topic conventions for chat messages, matching the topic used by
 * `@livekit/components-react` `useChat()` on the web. Required for
 * Web <-> Mobile interoperability in the same room.
 */
object ChatTopics {
    const val CHAT = "lk-chat-topic"
}
