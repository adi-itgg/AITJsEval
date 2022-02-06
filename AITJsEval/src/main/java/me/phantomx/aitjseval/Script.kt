package me.phantomx.aitjseval

import me.phantomx.aitjseval.listener.OnJavaScriptResponseListener

class Script(var tag: String, var script: String, var callback: OnJavaScriptResponseListener?) {
    var isCancelled = false
    var isError = false
    var status = ScriptStatus.QUEUE
    var result = ""
    var maxRetry = 3
    var loadFail = 0
    var timeout = 500

    override fun equals(other: Any?): Boolean {
        return if (other is Script) other.tag == tag else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + script.hashCode()
        result = 31 * result + (callback?.hashCode() ?: 0)
        result = 31 * result + isCancelled.hashCode()
        return result
    }
}