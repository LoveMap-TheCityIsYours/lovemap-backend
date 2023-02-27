package com.lovemap.lovemapbackend.lovespot.photo.like

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table("photo_like")
data class PhotoLike(
    @Id
    var id: Long = 0,

    @Column("photo_id")
    var photoId: Long,

    @Column("lover_id")
    var loverId: Long,

    @Column("happened_at")
    var happenedAt: Timestamp,

    @Column("like_or_dislike")
    var likeOrDislike: Int,

    @Column("love_location_id")
    var loveSpotId: Long
) {
    companion object {
        const val LIKE: Int = 1
        const val DISLIKE: Int = -1
    }
}


@Table("photo_likers_dislikers")
data class PhotoLikersDislikers(
    @Id
    var id: Long = 0,

    @Column("photo_id")
    var photoId: Long,

    @Column("likers")
    var likers: String? = null,

    @Column("dislikers")
    var dislikers: String? = null
) {
    fun getLikers(): Set<Long> {
        if (likers.isNullOrBlank()) {
            return emptySet()
        }
        return OBJECT_MAPPER.readValue(likers, object : TypeReference<Set<Long>>() {})
    }

    fun getDislikers(): Set<Long> {
        if (dislikers.isNullOrBlank()) {
            return emptySet()
        }
        return OBJECT_MAPPER.readValue(dislikers, object : TypeReference<Set<Long>>() {})
    }

    fun addLiker(loverId: Long): Set<Long> {
        val likers = getLikers().toMutableSet().also { it.add(loverId) }
        this.likers = OBJECT_MAPPER.writeValueAsString(likers)
        val dislikers = getDislikers().toMutableSet().also { it.remove(loverId) }
        this.dislikers = OBJECT_MAPPER.writeValueAsString(dislikers)
        return likers
    }

    fun addDisliker(loverId: Long): Set<Long> {
        val dislikers: MutableSet<Long> = getDislikers().toMutableSet().also { it.add(loverId) }
        this.dislikers = OBJECT_MAPPER.writeValueAsString(dislikers)
        val likers = getLikers().toMutableSet().also { it.remove(loverId) }
        this.likers = OBJECT_MAPPER.writeValueAsString(likers)
        return dislikers
    }

    fun removeLiker(loverId: Long) {
        val likers = getLikers().toMutableSet().also { it.remove(loverId) }
        this.likers = OBJECT_MAPPER.writeValueAsString(likers)
    }

    fun removeDisliker(loverId: Long) {
        val dislikers = getDislikers().toMutableSet().also { it.remove(loverId) }
        this.dislikers = OBJECT_MAPPER.writeValueAsString(dislikers)
    }

    companion object {
        private val OBJECT_MAPPER = ObjectMapper()
    }
}
