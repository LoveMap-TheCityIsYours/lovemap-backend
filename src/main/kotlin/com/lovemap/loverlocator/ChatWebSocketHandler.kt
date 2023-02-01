package com.lovemap.loverlocator

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.authentication.security.JwtService
import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import com.lovemap.loverlocator.chat.ChatMessage
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration


class ChatWebSocketHandler(
    private val jwtService: JwtService,
    private val cachedLoverService: CachedLoverService,
    private val objectMapper: ObjectMapper,
) : WebSocketHandler {

    private val logger = KotlinLogging.logger {}

    override fun handle(session: WebSocketSession): Mono<Void> {

        // should contain jwt and senderLoverId
        session.handshakeInfo.headers
        // register session in PeriodicMessageForwarderService
        // this service sends the unsent messages to senderLoverId on the WS session


        Flux.interval(Duration.ofSeconds(1)).doOnNext {
            // TODO: instead of this, read new messages from mongo and send those
            // but do this from PeriodicMessageForwarderService which will read all messages in batch
            // and send the messages to the corresponding WS sessions
            session.send(Mono.just(session.textMessage("hahaha $it")))
                .onErrorResume { e ->
                    logger.error(e) { "Error occurred In sender mono." }
                    session.close()
                }.subscribe()
        }.subscribe()


        val receiverMono: Mono<Void> = session.receive()
            .map { objectMapper.readValue(it.payloadAsText, ChatMessage::class.java) }
            .flatMap { msg ->
                if (jwtService.validateToken(msg.jwt)) {
                    validateLover(msg).doOnNext {
                        logger.info { "Received $msg" }

                        // TODO: instead of this, store the messages in mongo
                        sendMessageBack(session, msg)


                    }.doOnError { e ->
                        logger.error(e) { "Error occurred In receiver mono." }
                    }.then()
                } else {
                    Mono.error(LoveMapException(HttpStatus.UNAUTHORIZED, ErrorCode.Forbidden))
                }
            }.then()


        return receiverMono
    }

    private fun validateLover(msg: ChatMessage): Mono<ChatMessage> {
        return mono {
            val authentication = jwtService.getAuthentication(msg.jwt)
            val lover = cachedLoverService.getByUserName(authentication.name)
            if (lover.id != msg.senderLoverId) {
                Mono.error(LoveMapException(HttpStatus.UNAUTHORIZED, ErrorCode.Forbidden))
            } else {
                Mono.just(msg)
            }
        }.flatMap { it }
    }

    private fun sendMessageBack(
        session: WebSocketSession,
        msg: ChatMessage
    ) {
        session.send(Mono.just(session.textMessage("Received $msg")))
            .onErrorResume { e ->
                logger.error(e) { "Error occurred In sender mono." }
                session.close()
            }.subscribe()
    }
}
