package kotlinx.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.SerializersModule

class CsvFormat(val stringDeferringConfig: StringDeferringConfig, val csvConfig: CsvConfig = CsvConfig.default) :
    StringFormat {
    override val serializersModule: SerializersModule
        get() = stringDeferringConfig.serializersModule

    inner class TopListEncoder(val onComplete: (Map<String, String>) -> Unit) : AbstractEncoder() {
        override val serializersModule: SerializersModule
            get() = stringDeferringConfig.serializersModule

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            if(descriptor.kind != StructureKind.LIST) throw Error()
            return object : AbstractEncoder() {
                override val serializersModule: SerializersModule
                    get() = stringDeferringConfig.serializersModule

                override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
                    val og = StringDeferringEncoder(stringDeferringConfig, false)
                    return object : CompositeEncoder by og {
                        override fun endStructure(descriptor: SerialDescriptor) {
                            onComplete(og.map)
                        }
                    }
                }

                override fun encodeValue(value: Any) {
                    StringDeferringEncoder(stringDeferringConfig, false).encodeTaggedValue("", value)
                }
            }
        }

        override fun encodeValue(value: Any) = TODO()
    }

    inner class TopListDecoder(val maps: Iterator<Map<String, String>>) : AbstractDecoder() {
        override val serializersModule: SerializersModule
            get() = stringDeferringConfig.serializersModule

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
            if(descriptor.kind != StructureKind.LIST) throw IllegalStateException("TopListDecoder expected a list")
            return object : AbstractDecoder() {
                override val serializersModule: SerializersModule
                    get() = stringDeferringConfig.serializersModule

                override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
                    return StringDeferringDecoder(stringDeferringConfig, descriptor, currentMap) { "Record $index: " }
                }

                var index = 0
                var currentMap: Map<String, String> = emptyMap()
                override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (maps.hasNext()) {
                    currentMap = maps.next()
                    index++
                } else CompositeDecoder.DECODE_DONE

                override fun decodeNotNullMark(): Boolean = true  // TODO: Support null records?
                override fun decodeNull(): Nothing? = null
                override fun decodeBoolean(): Boolean = StringDeferringDecoder(
                    stringDeferringConfig,
                    Boolean.serializer().descriptor,
                    currentMap
                ) { "Record $index: " }.decodeBoolean()

                override fun decodeByte(): Byte =
                    StringDeferringDecoder(stringDeferringConfig, Byte.serializer().descriptor, currentMap) { "Record $index: " }.decodeByte()

                override fun decodeShort(): Short = StringDeferringDecoder(
                    stringDeferringConfig,
                    Short.serializer().descriptor,
                    currentMap
                ) { "Record $index: " }.decodeShort()

                override fun decodeInt(): Int =
                    StringDeferringDecoder(stringDeferringConfig, Int.serializer().descriptor, currentMap) { "Record $index: " }.decodeInt()

                override fun decodeLong(): Long =
                    StringDeferringDecoder(stringDeferringConfig, Long.serializer().descriptor, currentMap) { "Record $index: " }.decodeLong()

                override fun decodeFloat(): Float = StringDeferringDecoder(
                    stringDeferringConfig,
                    Float.serializer().descriptor,
                    currentMap
                ) { "Record $index: " }.decodeFloat()

                override fun decodeDouble(): Double = StringDeferringDecoder(
                    stringDeferringConfig,
                    Double.serializer().descriptor,
                    currentMap
                ) { "Record $index: " }.decodeDouble()

                override fun decodeChar(): Char =
                    StringDeferringDecoder(stringDeferringConfig, Char.serializer().descriptor, currentMap) { "Record $index: " }.decodeChar()

                override fun decodeString(): String = StringDeferringDecoder(
                    stringDeferringConfig,
                    String.serializer().descriptor,
                    currentMap
                ) { "Record $index: " }.decodeString()

                override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = decodeValue() as Int
                override fun decodeInline(descriptor: SerialDescriptor): Decoder = this
            }
        }

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int = TODO()
        override fun decodeValue(): Any = TODO()
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return when (deserializer.descriptor.kind) {
            is StructureKind.LIST -> deserializer.deserialize(
                TopListDecoder(
                    string.iterator().csvLines().asMaps(csvConfig).iterator()
                )
            )

            else -> decodeToSequence(string.iterator(), deserializer).single()
        }
    }


    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        return when (serializer.descriptor.kind) {
            StructureKind.LIST -> buildString {
                val maps = ArrayList<Map<String, String>>()
                serializer.serialize(TopListEncoder { maps += it }, value)
                appendCsv(maps.asSequence(), csvConfig)
            }

            else -> buildString {
                appendCsv(StringDeferringEncoder(stringDeferringConfig, steadyHeaders = false).apply {
                    encodeSerializableValue(serializer, value)
                }.map.let { sequenceOf(it) })
            }
        }
    }

    fun <T> encodeToAppendable(serializer: SerializationStrategy<T>, value: T, out: Appendable) {
        return when (serializer.descriptor.kind) {
            StructureKind.LIST -> {
                val maps = ArrayList<Map<String, String>>()
                serializer.serialize(TopListEncoder { maps += it }, value)
                out.appendCsv(maps.asSequence(), csvConfig)
            }

            else -> {
                out.appendCsv(StringDeferringEncoder(stringDeferringConfig, steadyHeaders = false).apply {
                    encodeSerializableValue(serializer, value)
                }.map.let { sequenceOf(it) })
            }
        }
    }

    fun <T> decodeToSequence(charIterator: CharIterator, deserializer: DeserializationStrategy<T>): Sequence<T> {
        return charIterator.csvLines(csvConfig).asMaps(csvConfig).map {
            StringDeferringDecoder(stringDeferringConfig, deserializer.descriptor, it).decodeSerializableValue(
                deserializer
            )
        }
    }

    @Deprecated("Use the official header, decodeToSequence", ReplaceWith("this.decodeToSequence(charIterator, deserializer))"))
    fun <T> decodeSequence(deserializer: DeserializationStrategy<T>, charIterator: CharIterator): Sequence<T> {
        return charIterator.csvLines(csvConfig).asMaps(csvConfig).map {
            StringDeferringDecoder(stringDeferringConfig, deserializer.descriptor, it).decodeSerializableValue(
                deserializer
            )
        }
    }

    /**
     * Begin serializing values into CSV records.
     *
     * @param serializer The serializer used to serialize the given object.
     * @param appendable The output where the CSV will be written.
     * @return a function that writes a T into the appendable
     */
    fun <T> beginEncodingToAppendable(serializer: SerializationStrategy<T>, out: Appendable): (T) -> Unit {
        val add = out.startCsv(
            keys = StringDeferringEncoder(stringDeferringConfig, steadyHeaders = true).headers(serializer.descriptor),
            config = csvConfig
        )
        return {
            add(StringDeferringEncoder(stringDeferringConfig, steadyHeaders = true).apply {
                encodeSerializableValue(
                    serializer,
                    it
                )
            }.map)
        }
    }

    /**
     * Serialize [values] into CSV record(s).
     *
     * @param serializer The serializer used to serialize the given object.
     * @param values The [Serializable] objects as a sequence.
     * @param appendable The output where the CSV will be written.
     */
    fun <T> encodeSequenceToAppendable(serializer: KSerializer<T>, values: Sequence<T>, appendable: Appendable) {
        appendable.appendCsv(
            keys = StringDeferringEncoder(stringDeferringConfig, steadyHeaders = true).headers(serializer.descriptor),
            values = values.map {
                StringDeferringEncoder(stringDeferringConfig, steadyHeaders = true).apply {
                    encodeSerializableValue(
                        serializer,
                        it
                    )
                }.map
            },
            config = csvConfig
        )
    }


}