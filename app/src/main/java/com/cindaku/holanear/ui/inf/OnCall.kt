package com.cindaku.holanear.ui.inf

interface OnCall {
    fun onTerminated()
    fun onEnded()
    fun onCallProgress(isVideo: Boolean)
    fun onRelayout(isVideo: Boolean)
    fun onSwitchVideoVoice(isVideo: Boolean)
}