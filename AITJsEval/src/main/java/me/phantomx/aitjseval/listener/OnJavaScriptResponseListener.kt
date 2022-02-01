package me.phantomx.aitjseval.listener

import me.phantomx.aitjseval.Script

interface OnJavaScriptResponseListener {
    fun onResponse(script: Script)
}