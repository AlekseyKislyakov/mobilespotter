package com.example.mobile_spotter.utils

import android.os.Handler
import java.util.*


object SessionHandlerImpl : SessionHandler {

    private var refreshTimer: Timer? = null

    private const val VISIBLE_SCREEN_BUT_INACTIVITY = 3 * 60 * 1000L // 3 min

    override var refreshListener: () -> Unit = {}

    private val handler = Handler()
    private var refreshTokenDelay: Long = 3 * 60 * 1000L // 10 min

    override fun onSessionRefreshed() {
        refreshTokenDelay = VISIBLE_SCREEN_BUT_INACTIVITY

        refreshTimer.destroy()

        refreshTimer = Timer().apply {
            schedule(refreshTokenDelay) {
                refreshListener.invoke()
            }
        }
    }

    override fun clearListeners() {
        refreshListener = {}
    }


    private fun Handler.postAndInvoke(block: () -> Unit) {
        post {
            block.invoke()
        }
    }

    private fun Timer?.destroy() {
        this?.let {
            it.cancel()
            it.purge()
        }
    }

    private fun Timer?.schedule(delay: Long, block: () -> Unit) {
        this?.schedule(object : TimerTask() {
            override fun run() {
                handler.postAndInvoke(block)
                this.cancel()
            }
        }, delay)
    }
}