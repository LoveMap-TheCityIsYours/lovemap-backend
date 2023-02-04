package com.lovemap.lovemapbackend.tracking

import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import java.time.Instant

interface UserTrackRepository : ReactiveMongoRepository<UserTrack, Long> {

    @Query(""" { 
        ${'$'}and: [ 
            { lastActivityAt: { ${'$'}lt: ?0 } },
            { ${'$'}or: [
               { lastActivityNotificationAt: { ${'$'}lt: ?0 } },
                { lastActivityNotificationAt: null }
            ] },
            { firebaseToken: { ${'$'}ne: null } }
        ] 
    } """)
    fun findByLastActivityAndNotificationBefore(instant: Instant): Flux<UserTrack>
}