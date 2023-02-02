package com.lovemap.lovemapbackend.tracking

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UserTrackRepository : ReactiveMongoRepository<UserTrack, Long>