package com.lovemap.lovemapbackend.email

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import com.mailjet.client.MailjetResponse
import com.mailjet.client.easy.MJEasyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class EmailService(
    @Value("classpath:/emails/password-reset.html") private val pwResetTemplate: Resource,
    private val emailProperties: MailjetProperties,
    private val asyncTaskService: AsyncTaskService
) {
    private val logger = KotlinLogging.logger {}

    suspend fun sendEmail() {
//        sendPasswordResetEmail(
//            loverService.unAuthorizedGetById(1),
//            UUID.randomUUID().toString().substringBefore("-").uppercase()
//        )
    }

    suspend fun sendPasswordResetEmail(lover: Lover, resetCode: String) {
        asyncTaskService.runBlockingAsync {
            try {
                var template = pwResetTemplate.inputStream.bufferedReader().use { it.readText() }
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
                logger.info { "Password reset email sent to ${lover.userName}. Response: [${response.data}]" }
            } catch (e: Exception) {
                logger.error(e) { "Error occurred during email sending." }
            }
        }
    }
}