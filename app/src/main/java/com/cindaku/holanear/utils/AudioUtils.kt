package com.cindaku.holanear.utils

import com.cindaku.holanear.BaseApp
import org.linphone.core.AudioDevice
import org.linphone.core.Call

class AudioUtils {
    companion object {
        private fun routeAudioTo(
            context: BaseApp,
            types: List<AudioDevice.Type>,
            call: Call? = null
        ) {
            val listSize = types.size
            val stringBuilder = StringBuilder()
            var index = 0
            while (index < listSize) {
                stringBuilder.append(types[index].name)
                if (index < listSize - 1) {
                    stringBuilder.append("/")
                }
                index++
            }
            val typesNames = stringBuilder.toString()
            context.appComponent.sipConnector().getCore()?.apply {
                if (this.callsNb == 0) {
                    return
                }
                val currentCall = call ?: this.currentCall ?: this.calls[0]
                val conference = this.conference

                for (audioDevice in this.audioDevices) {
                    if (types.contains(audioDevice.type) && audioDevice.hasCapability(AudioDevice.Capabilities.CapabilityPlay)) {
                        if (conference != null && conference.isIn) {
                            conference.outputAudioDevice = audioDevice
                        } else {
                            currentCall.outputAudioDevice = audioDevice
                        }
                        return
                    }
                }
            }
        }

        fun routeAudioToEarpiece(call: Call? = null) {

        }

        fun routeAudioToSpeaker(context: BaseApp, call: Call? = null) {
            routeAudioTo(context, arrayListOf(AudioDevice.Type.Speaker), call)
        }

        fun routeAudioToBluetooth(context: BaseApp, call: Call? = null) {
            routeAudioTo(context, arrayListOf(AudioDevice.Type.Bluetooth), call)
        }

        fun routeAudioToHeadset(context: BaseApp, call: Call? = null) {
            routeAudioTo(
                context,
                arrayListOf(AudioDevice.Type.Headphones, AudioDevice.Type.Headset),
                call
            )
        }

        fun isSpeakerAudioRouteCurrentlyUsed(context: BaseApp, call: Call? = null): Boolean {
            context.appComponent.sipConnector().getCore()?.apply {
                if (this.callsNb == 0) {
                    return false
                }
                val currentCall = call ?: this.currentCall ?: this.calls[0]
                val conference = this.conference

                val audioDevice =
                    if (conference != null && conference.isIn) conference.outputAudioDevice else currentCall.outputAudioDevice
                return audioDevice?.type == AudioDevice.Type.Speaker
            }
            return false
        }

        fun isBluetoothAudioRouteCurrentlyUsed(context: BaseApp, call: Call? = null): Boolean {
            context.appComponent.sipConnector().getCore()?.apply {
                if (this.callsNb == 0) {
                    return false
                }
                val currentCall = call ?: this.currentCall ?: this.calls[0]
                val conference = this.conference

                val audioDevice =
                    if (conference != null && conference.isIn) conference.outputAudioDevice else currentCall.outputAudioDevice
                return audioDevice?.type == AudioDevice.Type.Bluetooth
            }
            return false
        }

        fun isBluetoothAudioRouteAvailable(context: BaseApp): Boolean {
            context.appComponent.sipConnector().getCore()?.apply {
                for (audioDevice in this.audioDevices) {
                    if (audioDevice.type == AudioDevice.Type.Bluetooth &&
                        audioDevice.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
                    ) {
                        return true
                    }
                }
            }
            return false
        }

        fun isHeadsetAudioRouteAvailable(context: BaseApp): Boolean {
            context.appComponent.sipConnector().getCore()?.apply {
                for (audioDevice in this.audioDevices) {
                    if ((audioDevice.type == AudioDevice.Type.Headset || audioDevice.type == AudioDevice.Type.Headphones) &&
                        audioDevice.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
                    ) {
                        return true
                    }
                }
            }
            return false
        }
    }
}