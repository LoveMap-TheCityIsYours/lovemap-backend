package com.lovemap.lovemapbackend.lovespot.report

data class LoveSpotReportDto(
    val id: Long,
    val loverId: Long,
    val loveSpotId: Long,
    val reportText: String,
) {
    companion object {
        fun of(report: LoveSpotReport): LoveSpotReportDto {
            return LoveSpotReportDto(
                id = report.id,
                loverId = report.loverId,
                loveSpotId = report.loveSpotId,
                reportText = report.reportText,
            )
        }
    }
}

data class LoveSpotReportRequest(
    val loverId: Long,
    val loveSpotId: Long,
    val reportText: String,
)