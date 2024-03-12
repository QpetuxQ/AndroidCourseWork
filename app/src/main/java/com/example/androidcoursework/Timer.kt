package com.example.androidcoursework

import android.os.Handler
import android.os.Looper

class Timer(private val listener: OnTimeListener) {

    private val handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var isRunning = false

    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            handler.postDelayed(timerRunnable, TIMER_DELAY)
            isRunning = true
        }
    }

    fun pause() {
        if (isRunning) {
            handler.removeCallbacks(timerRunnable)
            isRunning = false
        }
    }

    fun stop() {
        if (isRunning) {
            handler.removeCallbacks(timerRunnable)
            isRunning = false
        }
        startTime = 0L
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsedTime = System.currentTimeMillis() - startTime
            val formattedTime = formatTime(elapsedTime)
            listener.onTime(formattedTime)
            handler.postDelayed(this, TIMER_DELAY)
        }
    }

    private fun formatTime(elapsedTime: Long): String {
        val seconds = elapsedTime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        val formattedSeconds = seconds % 60
        val formattedMinutes = minutes % 60
        val formattedHours = hours % 24

        return String.format("%02d:%02d:%02d", formattedHours, formattedMinutes, formattedSeconds)
    }

    companion object {
        private const val TIMER_DELAY = 100L
    }
}

interface OnTimeListener {
    fun onTime(duration: String)
}