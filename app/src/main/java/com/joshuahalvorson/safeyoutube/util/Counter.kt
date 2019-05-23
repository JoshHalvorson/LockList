package com.joshuahalvorson.safeyoutube.util

class Counter(private val start: Int, private var current: Int, private val maxValue: Int) {
    fun increment(): Int {
        if (current in start until maxValue) {
            return current++
        }
        current = start
        return current
    }

    fun getCurrent(): Int {
        return current
    }
}