package me.nluk.rozsada1.lib

import android.os.CountDownTimer

fun debounce(millis : Long = 500L, f : () -> Unit) : () -> Unit{
    var timer : CountDownTimer? = null
    return {
        timer?.cancel()
        timer = object : CountDownTimer(millis, 2 * millis) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                f()
            }
        }.start()
    }
}