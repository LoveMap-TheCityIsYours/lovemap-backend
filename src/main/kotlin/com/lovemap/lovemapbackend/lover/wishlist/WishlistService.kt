package com.lovemap.lovemapbackend.lover.wishlist

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.partnership.PartnershipService
import com.lovemap.lovemapbackend.utils.ErrorCode.AlreadyOnWishlist
import com.lovemap.lovemapbackend.utils.ErrorCode.WishlistElementNotFound
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class WishlistService(
    private val authorizationService: AuthorizationService,
    private val loveSpotService: LoveSpotService,
    private val partnershipService: PartnershipService,
    private val repository: WishlistElementRepository
) {

    suspend fun getWishList(loverId: Long): List<WishlistResponse> {
        authorizationService.checkAccessFor(loverId)
        val wishlistElements = repository.findByLoverId(loverId).toSet()

        val loveSpotIdsToWishlistElements: Map<Long, WishlistElement> =
            wishlistElements.associateBy({ it.loveSpotId }, { it })

        val loveSpots = loveSpotService.findAllByIds(loveSpotIdsToWishlistElements.keys)
        return convertToResponse(loveSpots, loveSpotIdsToWishlistElements)
    }

    private suspend fun convertToResponse(
        loveSpots: Flow<LoveSpot>,
        loveSpotIdsToWishlistElements: Map<Long, WishlistElement>
    ): List<WishlistResponse> {
        return loveSpots.map {
            WishlistResponse.of(
                wishlistElement = loveSpotIdsToWishlistElements[it.id] ?: throw LoveMapException(
                    NOT_FOUND,
                    WishlistElementNotFound
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
            saveWishlistElementForPartner(loverId, loveSpotId, addedAt)
            getWishList(loverId)
        }
    }

    private suspend fun saveWishlistElementForLover(loveSpotId: Long, loverId: Long, addedAt: Timestamp): Timestamp {
        loveSpotService.getById(loveSpotId)
        val wishlistElement = WishlistElement(
            loverId = loverId,
            loveSpotId = loveSpotId,
            addedAt = addedAt
        )
        repository.save(wishlistElement)
        return addedAt
    }

    private suspend fun saveWishlistElementForPartner(
        loverId: Long,
        loveSpotId: Long,
        addedAt: Timestamp
    ) {
        partnershipService.getPartnerOf(loverId)?.let { partner ->
            val partnerWishlistElement = WishlistElement(
                loverId = partner.id,
                loveSpotId = loveSpotId,
                addedAt = addedAt
            )
            repository.save(partnerWishlistElement)
        }
    }

    suspend fun removeFromWishlists(love: Love) {
        repository.findByLoverIdAndLoveSpotId(love.loverId, love.loveSpotId)?.let {
            repository.delete(it)
        }
        love.loverPartnerId?.let { loverPartnerId ->
            repository.findByLoverIdAndLoveSpotId(loverPartnerId, love.loveSpotId)?.let {
                repository.delete(it)
            }
        }
    }
}
