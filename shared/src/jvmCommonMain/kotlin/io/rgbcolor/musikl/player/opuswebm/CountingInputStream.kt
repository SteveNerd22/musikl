package io.rgbcolor.musikl.player.opuswebm

import java.io.InputStream

/**
 * Wrappa uno stream tenendo traccia di quanti byte sono stati letti finora,
 * e permette il seek (anche all'indietro) riaprendo la sorgente tramite una
 * factory che sa creare un InputStream a partire da un dato offset in byte
 * (tipicamente una richiesta HTTP con header Range).
 */
class CountingInputStream(
    private val streamFactory: (startByte: Long) -> InputStream,
    initialOffset: Long = 0,
) : InputStream() {

    private var delegate: InputStream = streamFactory(initialOffset)

    var bytesRead: Long = initialOffset
        private set

    override fun read(): Int {
        val b = delegate.read()
        if (b != -1) bytesRead++
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val n = delegate.read(b, off, len)
        if (n != -1) bytesRead += n
        return n
    }

    /** Riposiziona lo stream all'offset assoluto [targetBytePosition], riaprendo la sorgente. */
    fun seek(targetBytePosition: Long) {
        delegate.close()
        delegate = streamFactory(targetBytePosition)
        bytesRead = targetBytePosition
    }

    override fun close() = delegate.close()
}