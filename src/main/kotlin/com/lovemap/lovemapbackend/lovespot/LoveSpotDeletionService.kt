package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.love.LoveDeletionService
import com.lovemap.lovemapbackend.lover.wishlist.WishlistService
import com.lovemap.lovemapbackend.lovespot.photo.PhotoDeletionService
import com.lovemap.lovemapbackend.lovespot.report.LoveSpotReportService
import com.lovemap.lovemapbackend.newsfeed.NewsFeedDeletionService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoveSpotDeletionService(
    private val loveDeletionService: LoveDeletionService,
    private val loveSpotService: LoveSpotService,
    private val loveSpotReportService: LoveSpotReportService,
    private val photoDeletionService: PhotoDeletionService,
    private val wishlistService: WishlistService,
    private val newsFeedDeletionService: NewsFeedDeletionService
) {

    suspend fun deleteSpot(loveSpotId: Long): LoveSpot {
        val loveSpot = loveSpotService.getById(loveSpotId)
        loveSpotReportService.deleteReportsOfSpot(loveSpotId)
        photoDeletionService.deletePhotosForLoveSpot(loveSpot)
        loveDeletionService.deleteAllLovesAtSpot(loveSpot)
        wishlistService.deleteAllByLoveSpot(loveSpotId)
        loveSpotService.deleteLoveSpot(loveSpot)
        newsFeedDeletionService.deleteByTypeAndReferenceId(NewsFeedItem.Type.LOVE_SPOT, loveSpotId)
        return loveSpot
    }
}