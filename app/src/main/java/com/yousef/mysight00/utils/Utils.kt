package com.yousef.mysight00.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.yousef.mysight00.R

fun Context.showToast(message: String) {
    val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
    val view = toast.view
    view?.setBackgroundResource(R.drawable.toast_background)
    val textView = view?.findViewById<TextView>(android.R.id.message)
    textView?.setTextColor(ContextCompat.getColor(this, android.R.color.white))
    textView?.textSize = 18f
    textView?.gravity = Gravity.CENTER
    textView?.setShadowLayer(2f, 0f, 0f, android.R.color.black)
    view?.setPadding(48, 24, 48, 24)
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
} 