package com.lovemap.lovemapbackend.email

import com.mailjet.client.MailjetResponse
import com.mailjet.client.easy.MJEasyClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val emailProperties: EmailProperties
) {
    private val logger = KotlinLogging.logger {}

    suspend fun sendEmail() {
        withContext(Dispatchers.Default) {
            val client = MJEasyClient(
                emailProperties.apiKey,
                emailProperties.secretKey
            )
            val response: MailjetResponse = client.email()
                .from("noreply@lovemap.app", "LoveMap")
                .to("attila.palfi.92@gmail.com")
                .subject("Greetings from LoveMap.")
                .text("This is a welcome email bitch")
                .html("<h3>Dear passenger 1, welcome to <a href='https://www.mailjet.com/'>Mailjet</a>!</h3><br />May the delivery force be with you!")
                .customId("AppGettingStartedTest")
                .send()
            logger.info { "Email sent!!!! Response: [${response.data}]" }
        }
    }
}