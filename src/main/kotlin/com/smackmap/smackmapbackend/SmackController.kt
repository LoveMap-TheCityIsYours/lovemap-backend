package com.smackmap.smackmapbackend

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SmackController {

    @GetMapping("/smacks")
    fun getArticles(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(listOf("Smack 1", "Smack 2", "Smack 3"))
    }
}