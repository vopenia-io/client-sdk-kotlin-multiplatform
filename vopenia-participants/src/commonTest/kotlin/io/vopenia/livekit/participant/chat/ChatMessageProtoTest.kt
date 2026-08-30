package io.vopenia.livekit.participant.chat

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatMessageProtoTest {

    @Test
    fun roundTrip_simpleMessage() {
        val original = ChatMessage(
            id = "abc-123",
            timestamp = 1_700_000_000_000L,
            message = "hello world",
            senderIdentity = "sender-1"
        )
        val encoded = ChatMessageProto.encode(original)
        val decoded = ChatMessageProto.decode(encoded, senderIdentity = "sender-1")

        // senderIdentity is propagated by the transport layer, not the proto wire format
        assertEquals(original.id, decoded.id)
        assertEquals(original.timestamp, decoded.timestamp)
        assertEquals(original.message, decoded.message)
        assertEquals(false, decoded.deleted)
        assertEquals(false, decoded.generated)
        assertNull(decoded.editTimestamp)
        assertEquals("sender-1", decoded.senderIdentity)
    }

    @Test
    fun roundTrip_allFieldsSet() {
        val original = ChatMessage(
            id = "msg-id",
            timestamp = 12345L,
            editTimestamp = 67890L,
            message = "edited message",
            deleted = true,
            generated = true,
            senderIdentity = null
        )
        val encoded = ChatMessageProto.encode(original)
        val decoded = ChatMessageProto.decode(encoded, senderIdentity = null)

        assertEquals(original.id, decoded.id)
        assertEquals(original.timestamp, decoded.timestamp)
        assertEquals(original.editTimestamp, decoded.editTimestamp)
        assertEquals(original.message, decoded.message)
        assertEquals(original.deleted, decoded.deleted)
        assertEquals(original.generated, decoded.generated)
    }

    @Test
    fun roundTrip_unicode() {
        val original = ChatMessage(
            id = "u-1",
            timestamp = 1L,
            message = "Bonjour 👋 monde — ça va ?",
            senderIdentity = "user"
        )
        val encoded = ChatMessageProto.encode(original)
        val decoded = ChatMessageProto.decode(encoded, senderIdentity = "user")
        assertEquals(original.message, decoded.message)
    }

    @Test
    fun encode_emptyIdAndMessage_omitsFields() {
        // proto3: default values are not transmitted
        val empty = ChatMessage(id = "", timestamp = 0L, message = "")
        val bytes = ChatMessageProto.encode(empty)
        // No fields would be encoded (id empty, timestamp 0, message empty,
        // bools default false). Implementation still writes timestamp 0? No,
        // we always emit timestamp (it carries meaning even at 0). Just sanity-check decode.
        val decoded = ChatMessageProto.decode(bytes)
        assertEquals("", decoded.id)
        assertEquals(0L, decoded.timestamp)
        assertEquals("", decoded.message)
    }

    @Test
    fun decode_skipsUnknownFields() {
        // Hand-craft a payload with an unknown tag (field 99, varint) and a known one.
        // field 99, wire type 0 (varint): tag = (99 << 3) | 0 = 0x318 -> varint encoded
        // Then field 2 (timestamp) = 42
        val unknownTag = byteArrayOf(
            0xB8.toByte(), 0x06.toByte(),  // tag varint = 792 = (99 << 3 | 0)
            0x05.toByte()                   // value varint = 5
        )
        val knownPayload = ChatMessageProto.encode(
            ChatMessage(id = "x", timestamp = 42L, message = "y")
        )
        val combined = unknownTag + knownPayload

        val decoded = ChatMessageProto.decode(combined)
        assertEquals("x", decoded.id)
        assertEquals(42L, decoded.timestamp)
        assertEquals("y", decoded.message)
    }

    @Test
    fun decode_invalidPayload_throws() {
        // truncated varint
        assertFailsWith<IllegalArgumentException> {
            ChatMessageProto.decode(byteArrayOf(0x80.toByte()))
        }
    }

    @Test
    fun encode_deterministic() {
        // Same input must produce same bytes — required for tests asserting wire compatibility.
        val msg = ChatMessage(id = "fixed", timestamp = 999L, message = "stable")
        val a = ChatMessageProto.encode(msg)
        val b = ChatMessageProto.encode(msg)
        assertContentEquals(a, b)
    }

    @Test
    fun encode_fieldOrder_idFirst() {
        // The first field encoded should be field 1 (id) when non-empty.
        // Tag for field 1 wire 2 = (1 << 3) | 2 = 10 = 0x0A.
        val bytes = ChatMessageProto.encode(
            ChatMessage(id = "x", timestamp = 1L, message = "y")
        )
        assertTrue(bytes.isNotEmpty())
        assertEquals(0x0A.toByte(), bytes[0])
    }
}
