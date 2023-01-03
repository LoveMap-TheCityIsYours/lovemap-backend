package com.lovemap.lovemapbackend.lover.wishlist

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table("wishlist_element")
data class WishlistElement(
    @Id
    var id: Long = 0,

    @Column("lover_id")
    var loverId: Long,

    @Column("love_location_id")
    var loveSpotId: Long,

    @Column("added_at")
    var addedAt: Timestamp
)
