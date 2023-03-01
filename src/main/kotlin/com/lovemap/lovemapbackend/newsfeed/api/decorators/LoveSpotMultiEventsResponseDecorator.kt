package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.LoveSpotMultiEventsResponse
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.LoveSpotMultiEventsNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class LoveSpotMultiEventsResponseDecorator(
    private val loveSpotDecorator: LoveSpotNewsFeedResponseDecorator,
    private val loveDecorator: LoveNewsFeedResponseDecorator,
    private val reviewDecorator: LoveSpotReviewNewsFeedResponseDecorator,
    private val photoDecorator: LoveSpotPhotoNewsFeedResponseDecorator,
) : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.LOVE_SPOT_MULTI_EVENTS
    }

    override fun decorate(
        initialized: NewsFeedItemResponse,
        newsFeedData: NewsFeedData
    ): NewsFeedItemResponse {
        return if (newsFeedData is LoveSpotMultiEventsNewsFeedData) {
            initialized.copy(
                loveSpotMultiEvents = loveSpotMultiEventsResponse(initialized, newsFeedData)
            )
        } else {
            initialized
        }
    }

    private fun loveSpotMultiEventsResponse(
        initialized: NewsFeedItemResponse,
        newsFeedData: LoveSpotMultiEventsNewsFeedData
    ) = LoveSpotMultiEventsResponse(
        loveSpot = loveSpotDecorator.decorate(initialized, newsFeedData.loveSpot).loveSpot
            ?: throw LoveMapException(HttpStatus.NOT_FOUND, ErrorCode.LoveSpotNotFound),
        lovers = newsFeedData.lovers,
        loves = newsFeedData.loves.mapNotNull {
            loveDecorator.decorate(initialized, it).love
        },
        reviews = newsFeedData.reviews.mapNotNull {
            reviewDecorator.decorate(initialized, it).loveSpotReview
        },
        photos = newsFeedData.photos.mapNotNull {
            photoDecorator.decorate(initialized, it).loveSpotPhoto
        },
        loveSpotAddedHere = newsFeedData.loveSpotAddedHere
    )

}
