package com.lovemap.lovemapbackend

object TestUtils {

    fun <T> assertContains(collection: Collection<T>, vararg elements: T) {
        elements.forEach {
            kotlin.test.assertContains(collection, it)
        }
    }
}