package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.LoveSpotMultiEventsItem
import com.lovemap.lovemapbackend.newsfeed.api.LoveSpotMultiEventsResponse
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.data.*
import com.lovemap.lovemapbackend.newsfeed.processor.LoveSpotMultiEventsNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class LoveSpotMultiEventsResponseDecorator(
    private val loveSpotDecorator: LoveSpotNewsFeedResponseDecorator,
    private val loveDecorator: LoveNewsFeedResponseDecorator,
    private val loveSpotReviewDecorator: LoveSpotReviewNewsFeedResponseDecorator,
    private val loveSpotPhotoDecorator: LoveSpotPhotoNewsFeedResponseDecorator,
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
                loveSpotMultiEvents = LoveSpotMultiEventsResponse(
                    loveSpotEvents = newsFeedData.loveSpotEvents.map {
                        loveSpotMultiEventsItem(it, initialized)
                    }
                )
            )
        } else {
            initialized
        }
    }

    private fun loveSpotMultiEventsItem(
        it: ComparableNewsFeedData,
        initialized: NewsFeedItemResponse
    ): LoveSpotMultiEventsItem {
        return when (it) {
            is LoveSpotNewsFeedData -> LoveSpotMultiEventsItem(
                loveSpot = loveSpotDecorator.decorate(initialized, it).loveSpot
            )

            is LoveNewsFeedData -> LoveSpotMultiEventsItem(
                love = loveDecorator.decorate(initialized, it).love
            )

            is LoveSpotReviewNewsFeedData -> LoveSpotMultiEventsItem(
                loveSpotReview = loveSpotReviewDecorator.decorate(initialized, it).loveSpotReview
            )

            else -> LoveSpotMultiEventsItem(
                loveSpotPhoto = loveSpotPhotoDecorator.decorate(initialized, it).loveSpotPhoto
            )
        }
    }
}