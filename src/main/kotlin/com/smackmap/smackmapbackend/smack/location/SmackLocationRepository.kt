package com.smackmap.smackmapbackend.smack.location

import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface SmackLocationRepository : CoroutineSortingRepository<SmackLocation, Long> {


}
