package com.lightningkite.kotlinx.serialization.csv

import kotlin.math.min

/**
 * Configuration for CSV parsing and generation.
 *
 * This class defines the characters and behavior used for CSV operations, allowing customization
 * of separators, quote characters, and other CSV formatting options.
 *
 * @property fieldSeparator Character used to separate fields within a record (default is comma ',')
 * @property recordSeparator Character used to separate records (default is newline '\n')
 * @property optionalRecordSeparatorPrefix Optional character that may precede the record separator (default is carriage return '\r')
 * @property quoteCharacter Character used to quote fields that contain special characters (default is double quote '"')
 * @property defaultValue Default value used for missing fields (default is empty string "")
 * @property trimWhiteSpace Whether to trim leading and trailing whitespace from all values (default is true)
 */
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
        /**
         * Default configuration for CSV operations.
         * Uses standard CSV formatting with comma as separator, newline as record separator,
         * double quotes for quoting, and whitespace trimming enabled.
         */
        public val default: CsvConfig = CsvConfig()
    }
}

/**
 * Appends a sequence of maps as CSV data to this [Appendable].
 *
 * This function automatically extracts all unique keys from the maps to use as headers.
 * The first row will contain all the keys as headers, and subsequent rows will contain
 * the corresponding values from each map.
 *
 * @param values A sequence of maps where keys are column names and values are cell values
 * @param config The CSV configuration to use (defaults to [CsvConfig.default])
 */
public fun Appendable.appendCsv(values: Sequence<Map<String, String>>, config: CsvConfig = CsvConfig.default) {
    appendCsvRows(sequence {
        val keys = values.flatMap { it.keys }.distinct().toList()
        yield(keys)
        values.forEach { yield(keys.map { k -> it[k] ?: config.defaultValue }) }
    }, config)
}

/**
 * Appends a sequence of maps as CSV data to this [Appendable] using the specified keys as headers.
 *
 * This function uses the provided keys list to determine the order of columns in the CSV output.
 * The first row will contain the keys as headers, and subsequent rows will contain
 * the corresponding values from each map.
 *
 * @param keys The list of keys to use as headers in the CSV output
 * @param values A sequence of maps where keys are column names and values are cell values
 * @param config The CSV configuration to use (defaults to [CsvConfig.default])
 */
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

/**
 * Starts CSV output by writing the header row and returning a function to append data rows.
 *
 * This function writes the provided keys as a header row to the CSV output and returns
 * a function that can be called with maps to add data rows. This is useful for streaming
 * CSV generation where rows are added one at a time.
 *
 * @param keys The list of keys to use as headers in the CSV output
 * @param config The CSV configuration to use (defaults to [CsvConfig.default])
 * @return A function that takes a map and appends it as a row to the CSV output
 */
public fun Appendable.startCsv(
    keys: List<String>,
    config: CsvConfig = CsvConfig.default,
): (Map<String, String>) -> Unit {
    appendCsvRow(keys, config)
    return {
        appendCsvRow(keys.map { k -> it[k] ?: config.defaultValue })
    }
}

/**
 * Appends multiple rows of CSV data to this [Appendable].
 *
 * This function takes a sequence of string lists, where each list represents a row of CSV data,
 * and appends them to the output using the specified configuration.
 *
 * @param sequence A sequence of lists, where each list contains the values for a single CSV row
 * @param config The CSV configuration to use (defaults to [CsvConfig.default])
 */
public fun Appendable.appendCsvRows(sequence: Sequence<List<String>>, config: CsvConfig = CsvConfig.default) {
    sequence.forEach {
        appendCsvRow(it, config)
    }
}

/**
 * Appends a single row of CSV data to this [Appendable].
 *
 * This function takes a list of strings, where each string represents a field in a CSV row,
 * and appends them to the output as a single row using the specified configuration.
 * Fields are separated by the field separator defined in the configuration,
 * and the row is terminated with the record separator.
 *
 * @param row A list of strings representing the fields in a CSV row
 * @param config The CSV configuration to use (defaults to [CsvConfig.default])
 */
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

/**
 * Parses CSV data from this [CharIterator] and returns a sequence of rows.
 *
 * This function processes a character iterator containing CSV data and yields each row as a list of strings.
 * It handles quoted fields, field separators, and record separators according to the specified configuration.
 * The parser properly handles:
 * - Fields containing quotes, separators, and newlines
 * - Quoted fields with escaped quotes (doubled quotes)
 * - Optional record separator prefixes (like carriage returns before newlines)
 * - Whitespace trimming (if enabled in the configuration)
 *
 * @param config The CSV configuration to use for parsing (defaults to [CsvConfig.default])
 * @return A sequence of lists, where each list contains the fields of a single CSV row
 */
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
