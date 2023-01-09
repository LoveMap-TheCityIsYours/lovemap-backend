package com.lovemap.lovemapbackend.lover.wishlist

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.LoveSpotStatisticsService
import com.lovemap.lovemapbackend.newfeed.NewsFeedDeletionService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.partnership.PartnershipService
import com.lovemap.lovemapbackend.utils.ErrorCode.*
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class WishlistService(
    private val authorizationService: AuthorizationService,
    private val loveSpotService: LoveSpotService,
    private val partnershipService: PartnershipService,
    private val loveSpotStatisticsService: LoveSpotStatisticsService,
    private val repository: WishlistItemRepository,
    private val newsFeedDeletionService: NewsFeedDeletionService
) {

    suspend fun getWishList(loverId: Long): List<WishlistResponse> {
        authorizationService.checkAccessFor(loverId)
        val wishlistElements = repository.findByLoverId(loverId).toSet()

        val loveSpotIdsToWishlistElements: Map<Long, WishlistItem> =
            wishlistElements.associateBy({ it.loveSpotId }, { it })

        val loveSpots = loveSpotService.findAllByIds(loveSpotIdsToWishlistElements.keys)
        return convertToResponse(loveSpots, loveSpotIdsToWishlistElements)
    }

    private suspend fun convertToResponse(
        loveSpots: Flow<LoveSpot>,
        loveSpotIdsToWishlistElements: Map<Long, WishlistItem>
    ): List<WishlistResponse> {
        return loveSpots.map {
            WishlistResponse.of(
                wishlistItem = loveSpotIdsToWishlistElements[it.id] ?: throw LoveMapException(
                    NOT_FOUND,
                    WishlistItemNotFound
                ),
                loveSpot = it
            )
        }.toList()
    }

    suspend fun addToWishlist(loverId: Long, loveSpotId: Long): List<WishlistResponse> {
        authorizationService.checkAccessFor(loverId)
        return repository.findByLoverIdAndLoveSpotId(loverId, loveSpotId)?.let {
            throw LoveMapException(CONFLICT, AlreadyOnWishlist)
        } ?: run {
            val addedAt = Timestamp.from(Instant.now())
            saveWishlistElementForLover(loveSpotId, loverId, addedAt)
            val addedForPartner = saveWishlistElementForPartner(loverId, loveSpotId, addedAt)
            updateStatsForAddition(addedForPartner, loveSpotId)
            getWishList(loverId)
        }
    }

    private suspend fun saveWishlistElementForLover(loveSpotId: Long, loverId: Long, addedAt: Timestamp) {
        loveSpotService.getById(loveSpotId)
        val wishlistElement = WishlistItem(
            loverId = loverId,
            loveSpotId = loveSpotId,
            addedAt = addedAt
        )
        repository.save(wishlistElement)
    }

    private suspend fun saveWishlistElementForPartner(
        loverId: Long,
        loveSpotId: Long,
        addedAt: Timestamp
    ): Boolean {
        return partnershipService.getPartnerOf(loverId)?.let { partner ->
            if (!repository.existsByLoverIdAndLoveSpotId(partner.id, loveSpotId)) {
                val partnerWishlistElement = WishlistItem(
                    loverId = partner.id,
                    loveSpotId = loveSpotId,
                    addedAt = addedAt
                )
                repository.save(partnerWishlistElement)
                true
            } else {
                false
            }
        } ?: false
    }

    private suspend fun updateStatsForAddition(addedForPartner: Boolean, loveSpotId: Long) {
        val plusOccurrence = if (addedForPartner) {
            2
        } else {
            1
        }
        loveSpotStatisticsService.changeWishlistOccurrence(loveSpotId, plusOccurrence)
    }

    suspend fun removeFromWishlists(love: Love) {
        repository.findByLoverIdAndLoveSpotId(love.loverId, love.loveSpotId)?.let {
            repository.delete(it)
        }
        val removedForPartner: Boolean = love.loverPartnerId?.let { loverPartnerId ->
            repository.findByLoverIdAndLoveSpotId(loverPartnerId, love.loveSpotId)?.let {
                repository.delete(it)
                true
            }
        } ?: false
        updateStatsForRemoval(removedForPartner, love.loveSpotId)
    }

    private suspend fun updateStatsForRemoval(removedForPartner: Boolean, loveSpotId: Long) {
        val minusOccurrence = if (removedForPartner) {
            -2
        } else {
            -1
        }
        loveSpotStatisticsService.changeWishlistOccurrence(loveSpotId, minusOccurrence)
    }

    suspend fun deleteWishlistItem(loverId: Long, wishlistItemId: Long): List<WishlistResponse> {
        authorizationService.checkAccessFor(loverId)
        return repository.findById(wishlistItemId)?.let { wishlistItem ->
            if (wishlistItem.loverId != loverId) {
                throw LoveMapException(FORBIDDEN, Forbidden)
            }
            updateStatsForRemoval(false, wishlistItem.loveSpotId)
            repository.delete(wishlistItem)
            return getWishList(loverId)
        } ?: throw LoveMapException(NOT_FOUND, WishlistItemNotFound)
    }

    suspend fun deleteAllByLoveSpot(loveSpotId: Long) {
        repository.findAllByLoveSpotId(loveSpotId).collect { wishlistItem ->
            newsFeedDeletionService.deleteByTypeAndReferenceId(NewsFeedItem.Type.WISHLIST_ITEM, wishlistItem.id)
        }
        repository.deleteAllByLoveSpotId(loveSpotId)
    }

    fun getWishlistItemsFrom(generateFrom: Instant): Flow<WishlistItem> {
        return repository.findAllAfterAddedAt(Timestamp.from(generateFrom))
    }
}
