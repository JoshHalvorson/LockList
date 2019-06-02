package com.joshuahalvorson.safeyoutube.util

class Counter(var start: Int = 0, var current: Int = 0, var maxValue: Int = 0) {
    fun increment(): Int {
        if (current in start until maxValue) {
            return ++current
        }
        current = start
        return current
    }
}