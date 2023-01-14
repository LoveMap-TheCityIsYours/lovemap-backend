package com.lovemap.lovemapbackend.partnership

data class LoverPartnershipV2Response(
    val loverId: Long,
    val partnership: PartnershipResponse?
) {
    companion object {
        fun of(loverPartnerShips: LoverPartnerships): LoverPartnershipV2Response {
            return LoverPartnershipV2Response(
                loverId = loverPartnerShips.loverId,
                partnership = loverPartnerShips.partnerships.firstOrNull()
                    ?.let { PartnershipResponse.of(it) }
            )
        }
    }
}
