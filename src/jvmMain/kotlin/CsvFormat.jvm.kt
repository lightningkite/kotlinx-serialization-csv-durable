package com.lightningkite.kotlinx.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import java.io.InputStream
import java.io.Reader


public fun <T> CsvFormat.decodeFromReader(deserializer: DeserializationStrategy<T>, reader: Reader): T {
    return when (deserializer.descriptor.kind) {
        is StructureKind.LIST -> deserializer.deserialize(
            TopListDecoder(
                reader.iterator().csvLines(csvConfig).asMaps(csvConfig).iterator()
            )
        )

        else -> decodeToSequence(reader.iterator(), deserializer).single()
    }
}

internal fun Reader.iterator(): CharIterator = object: CharIterator() {
    var buffered = this@iterator.read()
    override fun hasNext(): Boolean = buffered != -1
    override fun nextChar(): Char {
        val result = buffered.toChar()
        buffered = this@iterator.read()
        return result
    }

}

/**
 * Transforms the given [stream] into lazily deserialized sequence of elements of type [T] using UTF-8 encoding and [deserializer].
 * Unlike [decodeFromStream], [stream] is allowed to have more than one element.
 *
 * Elements must all be of type [T].
 * Elements are parsed lazily when resulting [Sequence] is evaluated.
 * Resulting sequence is tied to the stream and can be evaluated only once.
 *
 * **Resource caution:** this method neither closes the [stream] when the parsing is finished nor provides a method to close it manually.
 * It is a caller responsibility to hold a reference to a stream and close it. Moreover, because stream is parsed lazily,
 * closing it before returned sequence is evaluated completely will result in [IOException] from decoder.
 *
 * @throws [SerializationException] if the given JSON input cannot be deserialized to the value of type [T].
 * @throws [IllegalArgumentException] if the decoded input cannot be represented as a valid instance of type [T]
 * @throws [IOException] If an I/O error occurs and stream cannot be read from.
 */
@ExperimentalSerializationApi
public fun <T> CsvFormat.decodeToSequence(stream: InputStream, deserializer: DeserializationStrategy<T>): Sequence<T> {
    return stream.reader().iterator().csvLines(csvConfig).asMaps(csvConfig).map {
        StringDeferringDecoder(stringDeferringConfig, deserializer.descriptor, it).decodeSerializableValue(
            deserializer
        )
    }
}

/**
 * Parse CSV line-by-line from the given [reader] into a sequence.
 *
 * @param deserializer The deserializer used to parse the given CSV string.
 * @param reader The CSV reader to parse.  This function *does not close the reader*.
 * @return A sequence of each element decoded.
 */
@ExperimentalSerializationApi
@Deprecated("Use the official header, decodeToSequence", ReplaceWith("this.decodeToSequence(reader.iterator(), deserializer))"))
public fun <T> CsvFormat.decodeSequenceFromReader(deserializer: KSerializer<T>, reader: Reader): Sequence<T> {
    return decodeToSequence(reader.iterator(), deserializer)
}

/**
 * Parse CSV from the given [reader] into a sequence of [Serializable] objects.
 * Designed to be comparable to [Reader.useLines].
 *
 * @param deserializer The deserializer used to parse the given CSV string.
 * @param reader The CSV reader to parse.
 * @param handler The code to handle the sequence of incoming values.  The sequence will not be available after the
 * function completes.
 */
@Deprecated("Use the official header, decodeToSequence", ReplaceWith("this.decodeToSequence(reader.iterator(), deserializer))"))
public fun <T> CsvFormat.decodeFromReaderUsingSequence(
    deserializer: KSerializer<T>,
    reader: Reader,
    handler: (Sequence<T>) -> Unit,
) {
    reader.use {
        handler(decodeToSequence(reader.iterator(), deserializer))
    }
}