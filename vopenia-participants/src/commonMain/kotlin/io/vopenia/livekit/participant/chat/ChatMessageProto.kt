package io.vopenia.livekit.participant.chat

/**
 * Hand-rolled protobuf encoder/decoder for the LiveKit `ChatMessage` message,
 * matching the wire format defined in `livekit_models.proto` and used by
 * `@livekit/components-react`'s `useChat()` on the web. Required for Web ↔ Mobile
 * interoperability when no native protobuf runtime is available (iOS via cinterop).
 *
 * ```
 * message ChatMessage {
 *   string id = 1;
 *   int64 timestamp = 2;
 *   optional int64 edit_timestamp = 3;
 *   string message = 4;
 *   bool deleted = 5;
 *   bool generated = 6;
 * }
 * ```
 */
internal object ChatMessageProto {

    fun encode(message: ChatMessage): ByteArray {
        val out = ArrayList<Byte>(message.id.length + message.message.length + 32)
        if (message.id.isNotEmpty()) writeString(out, fieldNumber = 1, value = message.id)
        writeVarintField(out, fieldNumber = 2, value = message.timestamp)
        message.editTimestamp?.let { writeVarintField(out, fieldNumber = 3, value = it) }
        if (message.message.isNotEmpty()) writeString(out, fieldNumber = 4, value = message.message)
        if (message.deleted) writeVarintField(out, fieldNumber = 5, value = 1L)
        if (message.generated) writeVarintField(out, fieldNumber = 6, value = 1L)
        return out.toByteArray()
    }

    fun decode(bytes: ByteArray, senderIdentity: String? = null): ChatMessage {
        var id = ""
        var timestamp = 0L
        var editTimestamp: Long? = null
        var message = ""
        var deleted = false
        var generated = false

        var offset = 0
        while (offset < bytes.size) {
            val (tag, afterTag) = readVarint(bytes, offset)
            offset = afterTag
            val fieldNumber = (tag shr 3).toInt()
            val wireType = (tag and 0x7L).toInt()
            when (fieldNumber) {
                1 -> {
                    require(wireType == 2) { "field 1 (id) expects wire type 2, got $wireType" }
                    val (s, end) = readLengthDelimitedString(bytes, offset)
                    id = s
                    offset = end
                }
                2 -> {
                    require(wireType == 0) { "field 2 (timestamp) expects wire type 0, got $wireType" }
                    val (v, end) = readVarint(bytes, offset)
                    timestamp = v
                    offset = end
                }
                3 -> {
                    require(wireType == 0) { "field 3 (edit_timestamp) expects wire type 0, got $wireType" }
                    val (v, end) = readVarint(bytes, offset)
                    editTimestamp = v
                    offset = end
                }
                4 -> {
                    require(wireType == 2) { "field 4 (message) expects wire type 2, got $wireType" }
                    val (s, end) = readLengthDelimitedString(bytes, offset)
                    message = s
                    offset = end
                }
                5 -> {
                    require(wireType == 0) { "field 5 (deleted) expects wire type 0, got $wireType" }
                    val (v, end) = readVarint(bytes, offset)
                    deleted = v != 0L
                    offset = end
                }
                6 -> {
                    require(wireType == 0) { "field 6 (generated) expects wire type 0, got $wireType" }
                    val (v, end) = readVarint(bytes, offset)
                    generated = v != 0L
                    offset = end
                }
                else -> offset = skipUnknownField(bytes, offset, wireType)
            }
        }

        return ChatMessage(
            id = id,
            timestamp = timestamp,
            editTimestamp = editTimestamp,
            message = message,
            deleted = deleted,
            generated = generated,
            senderIdentity = senderIdentity
        )
    }

    private fun writeString(out: ArrayList<Byte>, fieldNumber: Int, value: String) {
        val bytes = value.encodeToByteArray()
        writeVarint(out, ((fieldNumber.toLong()) shl 3) or 2L)
        writeVarint(out, bytes.size.toLong())
        bytes.forEach { out.add(it) }
    }

    private fun writeVarintField(out: ArrayList<Byte>, fieldNumber: Int, value: Long) {
        writeVarint(out, ((fieldNumber.toLong()) shl 3) or 0L)
        writeVarint(out, value)
    }

    private fun writeVarint(out: ArrayList<Byte>, value: Long) {
        var v = value
        while ((v and 0x7FL.inv()) != 0L) {
            out.add(((v and 0x7FL) or 0x80L).toByte())
            v = v ushr 7
        }
        out.add((v and 0x7FL).toByte())
    }

    private fun readVarint(bytes: ByteArray, start: Int): Pair<Long, Int> {
        var result = 0L
        var shift = 0
        var offset = start
        while (true) {
            require(offset < bytes.size) { "unexpected end of varint at $offset" }
            val b = bytes[offset].toInt() and 0xFF
            offset++
            result = result or ((b.toLong() and 0x7FL) shl shift)
            if (b and 0x80 == 0) break
            shift += 7
            require(shift <= 63) { "varint too long" }
        }
        return result to offset
    }

    private fun readLengthDelimitedString(bytes: ByteArray, start: Int): Pair<String, Int> {
        val (len, afterLen) = readVarint(bytes, start)
        val end = afterLen + len.toInt()
        require(end <= bytes.size) { "string length exceeds buffer" }
        return bytes.decodeToString(afterLen, end) to end
    }

    private fun skipUnknownField(bytes: ByteArray, start: Int, wireType: Int): Int = when (wireType) {
        0 -> readVarint(bytes, start).second
        1 -> start + 8
        2 -> {
            val (len, afterLen) = readVarint(bytes, start)
            afterLen + len.toInt()
        }
        5 -> start + 4
        else -> throw IllegalStateException("Unsupported wire type $wireType")
    }
}
