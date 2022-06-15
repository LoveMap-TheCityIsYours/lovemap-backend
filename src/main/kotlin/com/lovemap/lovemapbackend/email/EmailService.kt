package com.lovemap.lovemapbackend.email

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverService
import com.mailjet.client.MailjetResponse
import com.mailjet.client.easy.MJEasyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.async
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.*

@Service
class EmailService(
    @Value("classpath:/emails/password-reset.html") private val pwResetTemplate: Resource,
    private val emailProperties: EmailProperties,
    private val loverService: LoverService,
    private val blockingCoroutineDispatcher: ExecutorCoroutineDispatcher,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun sendEmail() {
        repeat(130) {
            sendPasswordResetEmail(
                loverService.unAuthorizedGetById(1),
                UUID.randomUUID().toString().substringBefore("-").uppercase()
            )
        }
    }

    suspend fun sendPasswordResetEmail(lover: Lover, resetCode: String) {
        CoroutineScope(Dispatchers.IO).async {
            var template = pwResetTemplate.file.readText()
            template = template.replace("{username}", lover.userName)
            template = template.replace("{resetCode}", resetCode)
            val client = MJEasyClient(
                emailProperties.apiKey,
                emailProperties.secretKey
            )
            val response: MailjetResponse = client.email()
                .from("noreply@lovemap.app", "LoveMap")
                .to(lover.email)
                .subject("Password reset for LoveMap")
                .html(template)
                .customId("PasswordReset")
                .send()
            logger.info { "Password reset email sent for $lover. Response: [${response.data}]" }
        }
    }
}