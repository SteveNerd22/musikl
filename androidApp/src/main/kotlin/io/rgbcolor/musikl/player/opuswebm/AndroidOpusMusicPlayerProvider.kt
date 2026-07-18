package io.rgbcolor.musikl.player.opuswebm

import android.media.AudioAttributes
import android.media.AudioTrack
import android.media.AudioFormat as AndroidAudioFormat

/** Implementazione Android del provider Opus/WebM, con output via android.media.AudioTrack. */
class AndroidOpusMusicPlayerProvider : BaseOpusMusicPlayerProvider() {

    override fun createAudioSink(sampleRate: Int, channels: Int): PcmAudioSink {
        val channelMask = if (channels >= 2) {
            AndroidAudioFormat.CHANNEL_OUT_STEREO
        } else {
            AndroidAudioFormat.CHANNEL_OUT_MONO
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelMask,
            AndroidAudioFormat.ENCODING_PCM_16BIT,
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AndroidAudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelMask)
                    .setEncoding(AndroidAudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize.coerceAtLeast(4096))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        return AudioTrackSink(audioTrack)
    }
}

private class AudioTrackSink(private val track: AudioTrack) : PcmAudioSink {
    override fun start() = track.play()

    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        track.write(bytes, offset, length)
    }

    override fun stop() = track.pause()

    override fun flush() = track.flush()

    override fun drain() {
        // AudioTrack non ha un vero drain() bloccante come SourceDataLine:
        // qui basta lasciare che lo stream audio hardware smaltisca il buffer
        // interno prima di stopparlo (già scritto tutto tramite write()).
    }

    override fun close() {
        track.stop()
        track.release()
    }
}