package com.smackmap.smackmapbackend.smack.location.review

import com.smackmap.smackmapbackend.smack.location.SmackLocation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SmackLocationReviewService {
    fun addReview(request: SmackLocationReviewRequest): SmackLocation {
        TODO("Not yet implemented")
    }
}