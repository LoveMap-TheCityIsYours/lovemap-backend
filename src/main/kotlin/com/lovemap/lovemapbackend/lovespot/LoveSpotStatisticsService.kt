package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.love.Love
import org.springframework.stereotype.Service

@Service
class LoveSpotStatisticsService(
    private val loveSpotService: LoveSpotService,
) {

    suspend fun recordLoveMaking(newLove: Love, countOfLovesAtSpot: Long): LoveSpot {
        val loveSpot = loveSpotService.getById(newLove.loveSpotId)
        setLastLoveAtForNew(loveSpot, newLove)
        setLastActiveAtForNew(loveSpot)
        loveSpot.numberOfLoves = countOfLovesAtSpot
        updatePopularity(loveSpot)
        return loveSpotService.save(loveSpot)
    }

    private fun setLastLoveAtForNew(
        loveSpot: LoveSpot,
        love: Love
    ) {
        loveSpot.lastLoveAt?.let {
            if (love.happenedAt.toInstant().isAfter(it.toInstant())) {
                loveSpot.lastLoveAt = love.happenedAt
            }
        } ?: run {
            loveSpot.lastLoveAt = love.happenedAt
        }
    }

    private fun setLastActiveAtForNew(loveSpot: LoveSpot) {
        loveSpot.lastActiveAt?.let {
            if (loveSpot.lastLoveAt!!.toInstant().isAfter(it.toInstant())) {
                loveSpot.lastActiveAt = loveSpot.lastLoveAt
            }
        } ?: run {
            loveSpot.lastActiveAt = loveSpot.lastLoveAt
        }
    }

    // popularity = 2 * number_of_loves + number_of_comments + occurrence_on_wishlists
    private fun updatePopularity(loveSpot: LoveSpot) {
        loveSpot.popularity = with(loveSpot) {
            2 * numberOfLoves + numberOfComments + occurrenceOnWishlists
        }
    }

    suspend fun deleteLoveMaking(deletedLove: Love, otherLovesAtSpot: List<Love>): LoveSpot {
        val loveSpot = loveSpotService.getById(deletedLove.loveSpotId)
        setLastLoveAtForDeleted(loveSpot, otherLovesAtSpot)
        setLastActiveAtForDeleted(loveSpot, otherLovesAtSpot)
        loveSpot.numberOfLoves = otherLovesAtSpot.size.toLong()
        updatePopularity(loveSpot)
        return loveSpotService.save(loveSpot)
    }

    private fun setLastLoveAtForDeleted(loveSpot: LoveSpot, otherLovesAtSpot: List<Love>) {
        if (otherLovesAtSpot.isNotEmpty()) {
            loveSpot.lastLoveAt = otherLovesAtSpot[0].happenedAt
        } else {
            loveSpot.lastLoveAt = null
        }
    }

    private fun setLastActiveAtForDeleted(loveSpot: LoveSpot, otherLovesAtSpot: List<Love>) {
        if (otherLovesAtSpot.isNotEmpty()) {
            loveSpot.lastActiveAt = otherLovesAtSpot[0].happenedAt
        } else {
            loveSpot.lastActiveAt = null
        }
    }
}