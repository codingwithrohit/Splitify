package com.example.splitify.data.remote.dto

import android.annotation.SuppressLint
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class TripMemberDto(
    val id: String,

    @SerialName("trip_id")
    val tripId: String,

    @SerialName("user_id")
    val userId: String? = null,  // NULL for guest members

    @SerialName("display_name")
    val displayName: String,

    val role: String,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("joined_at")
    val joinedAt: String? = null  // ISO timestamp from Supabase
)

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString()) // yyyy-MM-dd
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}



