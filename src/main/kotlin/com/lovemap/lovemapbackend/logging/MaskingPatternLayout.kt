package com.lovemap.lovemapbackend.logging

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import java.util.regex.Matcher
import java.util.regex.Pattern


class MaskingPatternLayout : PatternLayout() {
    private var patternsProperty: String? = null
    private var pattern: Pattern? = null

    fun getPatternsProperty(): String? {
        return patternsProperty
    }

    fun setPatternsProperty(patternsProperty: String?) {
        this.patternsProperty = patternsProperty
        if (this.patternsProperty != null) {
            this.pattern = patternsProperty?.let { Pattern.compile(it, Pattern.MULTILINE) }
        }
    }

    override fun doLayout(event: ILoggingEvent?): String? {
        val message = StringBuilder(super.doLayout(event))
        if (pattern != null) {
            val matcher: Matcher = pattern!!.matcher(message)
            while (matcher.find()) {
                var group = 1
                while (group <= matcher.groupCount()) {
                    if (matcher.group(group) != null) {
                        val startGrpIndex = matcher.start(group)
                        val endGrpIndex = matcher.end(group)
                        val diff = endGrpIndex - startGrpIndex + 1
                        val startIndex = startGrpIndex + diff
                        val endIndex1 = message.indexOf(",", startIndex)
                        val endIndex2 = message.indexOf(" ", startIndex)
                        val endIndex3 = message.indexOf(")", startIndex)
                        val endIndex4 = message.indexOf("\n", startIndex)
                        val endIndex = getSmallestInt(
                            listOf(
                                Integer.valueOf(endIndex1),
                                Integer.valueOf(endIndex2),
                                Integer.valueOf(endIndex3),
                                Integer.valueOf(endIndex4)
                            )
                        )
                        if (endIndex <= 0) {
                            continue
                        }
                        for (i in startIndex until endIndex) {
                            message.setCharAt(i, '*')
                        }
                    }
                    group++
                }
            }
        }
        return message.toString()
    }

    private fun getSmallestInt(integerList: List<Int>): Int {
        return integerList.stream().filter { integer: Int -> integer > 0 }
            .reduce { x: Int, y: Int -> if (x < y) x else y }.get()
    }
}