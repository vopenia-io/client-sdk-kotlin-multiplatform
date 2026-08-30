package io.vopenia.livekit.participant.data

data class DataPacket(
    val payload: ByteArray,
    val topic: String? = null,
    val senderIdentity: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataPacket) return false

        if (!payload.contentEquals(other.payload)) return false
        if (topic != other.topic) return false
        if (senderIdentity != other.senderIdentity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = payload.contentHashCode()
        result = 31 * result + (topic?.hashCode() ?: 0)
        result = 31 * result + (senderIdentity?.hashCode() ?: 0)
        return result
    }
}
