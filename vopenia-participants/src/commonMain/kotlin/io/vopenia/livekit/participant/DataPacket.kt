package io.vopenia.livekit.participant

data class DataPacket(
    val data: ByteArray,
    val senderIdentity: String?,
    val topic: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataPacket) return false
        return data.contentEquals(other.data) &&
            senderIdentity == other.senderIdentity &&
            topic == other.topic
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + (senderIdentity?.hashCode() ?: 0)
        result = 31 * result + (topic?.hashCode() ?: 0)
        return result
    }
}
