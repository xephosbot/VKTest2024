package com.xbot.clockview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

internal class Clock(
    private val scope: CoroutineScope,
    private val zoneId: ZoneId
) {
    var hours: Int = 0
        private set
    var minutes: Int = 0
        private set
    var seconds: Int = 0
        private set

    private var job: Job? = null
    private var action: (() -> Unit)? = null

    fun doOnTimeTick(action: () -> Unit) {
        this.action = action
    }

    fun start() {
        job = scope.launch {
            while (isActive) {
                delayToNextInterval(1000L)
                updateTime()
                action?.invoke()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    init {
        updateTime()
    }

    private fun updateTime() {
        val now = ZonedDateTime.now(zoneId)
        hours = now.hour
        minutes = now.minute
        seconds = now.second
    }

    override fun toString(): String {
        return "Clock: h: $hours, m: $minutes, s: $seconds"
    }
}

internal suspend fun delayToNextInterval(intervalMillis: Long) {
    val currentMillis = System.currentTimeMillis()
    val delayMillis = intervalMillis - currentMillis % intervalMillis
    delay(delayMillis)
}
