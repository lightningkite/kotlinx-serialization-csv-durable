package com.lightningkite.kotlinx.serialization.csv

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals

class DemoTest {

    val stringDeferringConfig = StringDeferringConfig(EmptySerializersModule())
    val csv = CsvFormat(stringDeferringConfig)

    @Serializable
    data class Vehicle(
        val year: Int,
        val make: String,
        val model: String,
        val trim: String? = null,
        val owner: OwnerInfo? = null,
        val packages: List<String> = listOf(),
    )

    @Serializable
    data class OwnerInfo(
        val name: String,
        val email: String
    )

    @Test fun testInput() {
        // You can mix ways of defining fields.
        // Complex fields can be encoded OR be split across columns.
        val result = csv.decodeFromString<List<Vehicle>>("""
            year,make,model,trim,owner.name,owner.email,packages,packages.0,packages.1,packages.2,owner
            1990,Saturn,SL2,Trim,Owner Man,ownerman@gmail.com,,A,B,C,
            1991,Saturn,SL2,Unowned,,,,A,,,
            1992,Saturn,SL2,null,,,,,,,
            1993,Saturn,SL2,Trim,Owner Man,ownerman@gmail.com,"%[""A"",""B"",""C""]",,,,
            1994,Saturn,SL2,Unowned,,,"%[""A""]",,,,
            1995,Saturn,SL2,null,,,%[],,,,
        """.trimIndent()).also(::println)
        assertEquals(listOf(
            Vehicle(1990, "Saturn", "SL2", "Trim", OwnerInfo("Owner Man", "ownerman@gmail.com"), packages = listOf("A", "B", "C")),
            Vehicle(1991, "Saturn", "SL2", "Unowned", null, packages = listOf("A")),
            Vehicle(1992, "Saturn", "SL2", null, null, packages = listOf()),
            Vehicle(1993, "Saturn", "SL2", "Trim", OwnerInfo("Owner Man", "ownerman@gmail.com"), packages = listOf("A", "B", "C")),
            Vehicle(1994, "Saturn", "SL2", "Unowned", null, packages = listOf("A")),
            Vehicle(1995, "Saturn", "SL2", null, null, packages = listOf()),
        ).also { it.joinToString("\n").let(::println) }, result.also { it.joinToString("\n").let(::println) })
    }

    @Test fun testOutput() {
        csv.encodeToString(listOf(
            Vehicle(1990, "Saturn", "SL2", "Trim", OwnerInfo("Owner Man", "ownerman@gmail.com"), packages = listOf("A", "B", "C")),
            Vehicle(1991, "Saturn", "SL2", "Unowned", null, packages = listOf("A")),
            Vehicle(1992, "Saturn", "SL2", null, null, packages = listOf()),
        )).let(::println)
        /* OUTPUTS:
            year,make,model,trim,owner.name,owner.email,packages.0,packages.1,packages.2,owner
            1990,Saturn,SL2,Trim,Owner Man,ownerman@gmail.com,A,B,C,
            1991,Saturn,SL2,Unowned,,,A,,,null
            1992,Saturn,SL2,null,,,,,,null
         */
    }

    @Test fun testStreamOutput() {
        val out = StringBuilder()
        csv.encodeSequenceToAppendable(Vehicle.serializer(), sequenceOf(
            Vehicle(1990, "Saturn", "SL2", "Trim", OwnerInfo("Owner Man", "ownerman@gmail.com"), packages = listOf("A", "B", "C")),
            Vehicle(1991, "Saturn", "SL2", "Unowned", null, packages = listOf("A")),
            Vehicle(1992, "Saturn", "SL2", null, null, packages = listOf()),
        ), out)
        println(out)
        /* OUTPUTS:
            year,make,model,trim,owner,owner.name,owner.email,packages
            1990,Saturn,SL2,Trim,,Owner Man,ownerman@gmail.com,"%[""A"",""B"",""C""]"
            1991,Saturn,SL2,Unowned,null,,,"%[""A""]"
            1992,Saturn,SL2,null,null,,,%[]
         */
    }
}