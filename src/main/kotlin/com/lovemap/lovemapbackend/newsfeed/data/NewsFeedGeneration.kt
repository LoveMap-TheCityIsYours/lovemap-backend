package com.lovemap.lovemapbackend.newsfeed.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table("news_feed_generation")
data class NewsFeedGeneration(
    @Id
    var id: Long = 0,

    @Column("generated_at")
    var generatedAt: Timestamp,

    @Column("generated_items")
    var generatedItems: Long = 0,

    @Column("generation_duration_milliseconds")
    var generationDurationMs: Long = 0
)
