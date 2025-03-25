package com.lightningkite.kotlinx.serialization.csv

import kotlin.math.min

public data class CsvConfig(
    public val fieldSeparator: Char = ',',
    public val recordSeparator: Char = '\n',
    public val optionalRecordSeparatorPrefix: Char = '\r',
    public val quoteCharacter: Char = '"',
    public val defaultValue: String = "",
    public val trimWhiteSpace: Boolean = true, // Will remove any leading and tailing white space from ALL values.
) {
    internal val quoteCharacterString = "$quoteCharacter"
    internal val quoteCharacterString2 = "$quoteCharacter$quoteCharacter"

    public companion object {
        public val default: CsvConfig = CsvConfig()
    }
}


public fun Appendable.appendCsv(values: Sequence<Map<String, String>>, config: CsvConfig = CsvConfig.default) {
    appendCsvRows(sequence {
        val keys = values.flatMap { it.keys }.distinct().toList()
        yield(keys)
        values.forEach { yield(keys.map { k -> it[k] ?: config.defaultValue }) }
    }, config)
}

public fun Appendable.appendCsv(
    keys: List<String>,
    values: Sequence<Map<String, String>>,
    config: CsvConfig = CsvConfig.default,
) {
    appendCsvRows(sequence {
        yield(keys)
        values.forEach { yield(keys.map { k -> it[k] ?: config.defaultValue }) }
    }, config)
}

public fun Appendable.startCsv(
    keys: List<String>,
    config: CsvConfig = CsvConfig.default,
): (Map<String, String>) -> Unit {
    appendCsvRow(keys, config)
    return {
        appendCsvRow(keys.map { k -> it[k] ?: config.defaultValue })
    }
}

public fun Appendable.appendCsvRows(sequence: Sequence<List<String>>, config: CsvConfig = CsvConfig.default) {
    sequence.forEach {
        appendCsvRow(it, config)
    }
}

public fun Appendable.appendCsvRow(row: List<String>, config: CsvConfig = CsvConfig.default) {
    var first = true
    for (value in row) {
        if (first) first = false
        else this.append(config.fieldSeparator)
        appendCsvEscaped(value, config)
    }
    this.append(config.recordSeparator)
}

internal fun Appendable.appendCsvEscaped(value: String, config: CsvConfig = CsvConfig.default) {
    if (value.contains(config.fieldSeparator) || value.contains(config.recordSeparator) || value.contains(config.quoteCharacter)) {
        this.append(config.quoteCharacter)
        this.append(value.replace(config.quoteCharacterString, config.quoteCharacterString2))
        this.append(config.quoteCharacter)
    } else {
        this.append(value)
    }
}

internal fun Sequence<List<String>>.asMaps(config: CsvConfig = CsvConfig.default): Sequence<Map<String, String>> {
    return sequence {
        val iter = this@asMaps.iterator()
        if (!iter.hasNext()) return@sequence
        val keys = iter.next().map { it.trim() }
        while (iter.hasNext()) {
            val values = iter.next()
            yield((0 until min(keys.size, values.size))
                .asSequence()
                .filter { values[it] != config.defaultValue }
                .associate { keys[it] to values[it] })
        }
    }
}

public fun CharIterator.csvLines(config: CsvConfig = CsvConfig.default): Sequence<List<String>> = sequence {
    val builder = StringBuilder("")
    var inQuotes = false
    var lastWasQuote = false
    var listBuilder = ArrayList<String>()
    var lastWasNewline = true
    var lastWasPrefix = false

    fun addToList(value:String){
        if(config.trimWhiteSpace)
            listBuilder.add(value.trim())
        else
            listBuilder.add(value)
    }

    this@csvLines.forEach {
        if (lastWasPrefix && it != config.recordSeparator) builder.append(config.optionalRecordSeparatorPrefix)
        lastWasPrefix = false
        if (lastWasQuote && it == config.quoteCharacter) {
            if (!inQuotes) {
                builder.append(config.quoteCharacter)
            }
            inQuotes = !inQuotes
            return@forEach
        }
        lastWasQuote = it == config.quoteCharacter
        if (inQuotes) {
            if (it == config.quoteCharacter) {
                inQuotes = false
            } else {
                builder.append(it)
            }
        } else {
            if (it == config.quoteCharacter) {
                inQuotes = true
            } else if (it == config.fieldSeparator) {
                lastWasNewline = false
                addToList(builder.toString())
                builder.clear()
            } else if (it == config.optionalRecordSeparatorPrefix) {
                // ignore
                lastWasPrefix = true
            } else if (it == config.recordSeparator) {
                if (!lastWasNewline) {
                    addToList(builder.toString())
                    builder.clear()
                    yield(listBuilder)
                    listBuilder = ArrayList()
                }
                lastWasNewline = true
            } else {
                lastWasNewline = false
                builder.append(it)
            }
        }
    }
    if (!lastWasNewline) {
        addToList(builder.toString())
        builder.clear()
        yield(listBuilder)
        listBuilder = ArrayList()
    }
}
