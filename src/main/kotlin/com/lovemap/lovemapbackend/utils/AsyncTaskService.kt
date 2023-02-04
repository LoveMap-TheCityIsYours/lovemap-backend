package com.lovemap.lovemapbackend.utils

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AsyncTaskService {
    private val logger = KotlinLogging.logger {}

    fun runBlockingAsync(runnable: Runnable) {
        logger.info { "Entering async context" }
        CoroutineScope(Dispatchers.IO).async {
            logger.info { "Executing async task" }
            runnable.run()
        }
    }

    suspend fun <T> runAsync(function: suspend () -> T): Deferred<T> {
        logger.info { "Entering async context" }
        return CoroutineScope(Dispatchers.IO).async {
            logger.info { "Executing async task" }
            function.invoke()
        }
    }
}
