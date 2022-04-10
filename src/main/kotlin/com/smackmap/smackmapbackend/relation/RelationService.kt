package com.smackmap.smackmapbackend.relation

import com.smackmap.smackmapbackend.smacker.SmackerService
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RelationService(
    private val smackerService: SmackerService,
    private val relationRepository: RelationRepository
) {
    private val logger = KotlinLogging.logger {}
}