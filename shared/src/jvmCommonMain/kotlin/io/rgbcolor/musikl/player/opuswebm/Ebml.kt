package io.rgbcolor.musikl.player.opuswebm

import java.io.EOFException
import java.io.InputStream

/**
 * Risultato della lettura di un VINT (variable-length integer) EBML:
 * il valore decodificato e quanti byte sono stati consumati dallo stream.
 */
data class VintResult(val value: Long, val lengthInBytes: Int)

/**
 * Header di un elemento EBML: ID, size dichiarata (o null se "unknown size",
 * tipico per Segment/Cluster in stream non finalizzati), e lunghezza totale
 * dell'header (ID + size) in byte, utile per calcolare offset.
 */
data class ElementHeader(
    val id: Long,
    val size: Long?, // null = unknown size ("leggi finché non trovi il prossimo elemento valido / EOF")
    val headerLength: Int,
)

object Ebml {

    /**
     * Legge un VINT da stream.
     *
     * @param keepMarker se true (usato per gli ID) mantiene il bit di marker
     *   dentro il valore risultante, come da convenzione Matroska/EBML per gli ID.
     *   Se false (usato per le size) lo maschera via, lasciando solo il valore numerico.
     */
    fun readVint(input: InputStream, keepMarker: Boolean): VintResult {
        val first = input.read()
        if (first == -1) throw EOFException("stream finito leggendo un vint")

        var mask = 0x80
        var length = 1
        while (length <= 8 && (first and mask) == 0) {
            mask = mask ushr 1
            length++
        }
        if (length > 8) throw IllegalStateException("vint non valido, primo byte=$first")

        var value = if (keepMarker) {
            first.toLong()
        } else {
            (first and (mask - 1)).toLong()
        }

        for (i in 1 until length) {
            val b = input.read()
            if (b == -1) throw EOFException("stream finito a metà di un vint")
            value = (value shl 8) or b.toLong()
        }

        return VintResult(value, length)
    }

    /**
     * Legge l'header di un elemento (ID + size). Rileva anche il caso
     * "unknown size" (tutti i bit del valore a 1), ritornando size=null.
     */
    fun readElementHeader(input: InputStream): ElementHeader {
        val idResult = readVint(input, keepMarker = true)
        val sizeResult = readVint(input, keepMarker = false)

        val allOnes = (1L shl (7 * sizeResult.lengthInBytes)) - 1
        val size = if (sizeResult.value == allOnes) null else sizeResult.value

        return ElementHeader(
            id = idResult.value,
            size = size,
            headerLength = idResult.lengthInBytes + sizeResult.lengthInBytes,
        )
    }

    /** Scarta esattamente [n] byte dallo stream, gestendo il fatto che skip() non è garantito. */
    fun skipExact(input: InputStream, n: Long) {
        var remaining = n
        val buffer = ByteArray(8192)
        while (remaining > 0) {
            val toRead = minOf(remaining, buffer.size.toLong()).toInt()
            val read = input.read(buffer, 0, toRead)
            if (read == -1) throw EOFException("stream finito mentre si scartavano $n byte")
            remaining -= read
        }
    }

    /** Legge esattamente [n] byte e li ritorna come ByteArray. */
    fun readExact(input: InputStream, n: Int): ByteArray {
        val out = ByteArray(n)
        var offset = 0
        while (offset < n) {
            val read = input.read(out, offset, n - offset)
            if (read == -1) throw EOFException("stream finito leggendo $n byte")
            offset += read
        }
        return out
    }
}