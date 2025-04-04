package com.lightningkite.kotlinx.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail

class CSV2Test {
    @Test
    fun testCsvLinesBasic() {
        assertEquals(
            listOf(
                listOf("a", "b", "c"),
                listOf("d", "e", "f"),
            ),
            "a,b,c\nd,e,f".iterator().csvLines().toList()
        )
    }

    @Test
    fun testCsvLines() {
        assertEquals(
            listOf(
                listOf("Test", "Outside", "Quote\"Escape", "retarded\nquoting", "more"),
                listOf("why"),
            ),
            "\"Test\",Outside,\"Quote\"\"Escape\",\"retarded\nquoting\",more\r\n\nwhy".iterator().csvLines().toList()
        )
    }

    @Test
    fun testCsvLinesTrimmedValues() {
        val expected = listOf(
            listOf("Test", "Outside", "Quote\"Escape", "retarded\nquoting", "more"),
            listOf("why"),
        )
        val input = "\"Test\",Outside,\"Quote\"\"Escape\",\"retarded\nquoting\",more\r\n\n  \t\t why  \t\t  "
        assertEquals(
            expected ,
            input.iterator().csvLines(CsvConfig.default.copy(trimWhiteSpace = true)).toList()
        )
        assertNotEquals(
            expected,
            input.iterator().csvLines(CsvConfig.default.copy(trimWhiteSpace = false)).toList()
        )
    }

    @Test
    fun testCsvLines2() {
        """
auctionDay,auction,venue,company,auctionDate,city,state,country,wasMigrated,lotNumber,sortOrder,lotType,year,make,model,bodyStyle,trim,engine,provenance,interiorColor,exteriorColor,odometer,odometer.amount,odometer.unit,vin,link,externalImages,charity,custom,ignoreInPortfolios,sublots,reserve,estimatedHighPrice,estimatedHighPrice.without.currency,estimatedHighPrice.without.original,estimatedLowPrice,estimatedLowPrice.without.currency,estimatedLowPrice.without.original
50689556-3029-4bf0-b0da-fba59ec92a98,5b00c821-63e0-4001-8ae5-62cf3abbf8c0,bcfdb127-00dd-463f-882a-b15f928b2f9a,92cccbae-3eb6-4f2a-8392-c395ec759c28,2024-03-18,Scottsdale,AZ,US,FALSE,4,4,8571fa3e-1dff-4c9b-9d14-c9c80d449bf2,1987,Chevrolet,Camaro,RS,Grey,,,Grey,Candy Holly Green,TRUE,131060,Miles,1G1FP21H8HL130505,https://www.ebay.com/itm/235435071796?itmmeta=01HS1Q82BM8VW83EY29DKQY8NX&hash=item36d1061934:g:RZ8AAOSwaGNlz9co,"[""https://i.ebayimg.com/images/g/S0IAAOSwGG5lz9cq/s-l960.jpg""]",FALSE,FALSE,FALSE,[],TRUE,TRUE,USD,19999.99,FALSE,,

        """.trimIndent().iterator().csvLines().asMaps().single().entries.forEach {
            println("${it.key} = ${it.value}")
        }
    }

    @Test
    fun testAsMaps() {
        assertEquals(
            listOf(
                mapOf("a" to "1", "b" to "2", "c" to "3"),
                mapOf("a" to "4", "b" to "5", "c" to "6"),
            ),
            sequenceOf(listOf("a", "b", "c"), listOf("1", "2", "3"), listOf("4", "5", "6")).asMaps().toList()
        )
    }

    @Test
    fun testAsMapsTrimmedHeaders() {
        assertEquals(
            listOf(
                mapOf("a" to "1", "b" to "2", "c" to "3"),
                mapOf("a" to "4", "b" to "5", "c" to "6"),
            ),
            sequenceOf(listOf("a   ", "   b", " \tc\t\t "), listOf("1", "2", "3"), listOf("4", "5", "6")).asMaps().toList()
        )
    }

    @Test
    fun emitCsvEscaped() {
        assertEquals("test", buildString { appendCsvEscaped("test") })
        assertEquals("\"te\"\"st\"", buildString { appendCsvEscaped("te\"st") })
        assertEquals("\"te,st\"", buildString { appendCsvEscaped("te,st") })
        assertEquals("\"te\nst\"", buildString { appendCsvEscaped("te\nst") })
    }

    @Test
    fun emitCsv() {
        assertEquals("""
            a,b,c
            1,2,3
            4,5,6
            
        """.trimIndent(), buildString {
            startCsv(listOf("a", "b", "c")).let {
                it(mapOf("a" to "1", "b" to "2", "c" to "3"))
                it(mapOf("a" to "4", "b" to "5", "c" to "6"))
            }
        })
    }

    @Serializable
    data class TestObj(
        val x: Int = 0,
        val y: String = "asdf",
        val z: String? = null,
        val a: TestObj? = null,
        val b: List<Int> = listOf(),
        val c: Map<String, Int> = mapOf(),
    )

    @Serializable
    data class TestWeirdCases(
        val title: String,
        val blankable: String,
        val nullable: Int?
    )

    val stringDeferringConfig = StringDeferringConfig(EmptySerializersModule())
    val csv = CsvFormat(stringDeferringConfig)
    val formats = listOf(csv)
    val basis = TestObj(
        x = 1,
        y = "fdsa",
        z = "notnull",
        a = TestObj(x = 42, a = TestObj(x = -1)),
        b = listOf(1),
        c = mapOf("key" to 1)
    )
    val basisButNull = basis.copy(a = null)

    @Test
    fun alternatives() {
        val format = CsvFormat(stringDeferringConfig)
        assertEquals(
            listOf(
                TestWeirdCases("Normal", "asdf", 1),
                TestWeirdCases("NormalNull", "asdf", null),
                TestWeirdCases("BlankAndNull", "", null),
                TestWeirdCases("BlankAndNull2", "", null),
            ),
            format.decodeFromString("""
                title,blankable,nullable
                Normal,asdf,1
                NormalNull,asdf,null
                BlankAndNull,,
                BlankAndNull2
            """.trimIndent())
        )
        assertEquals(
            listOf(
                TestWeirdCases("BlankAndNull2", "", null),
            ),
            format.decodeFromString("""
                title
                BlankAndNull2
            """.trimIndent())
        )
    }

    @Test
    fun testParse() {
        val serializer = TestObj.serializer()
        assertEquals(
            basis,
            StringDeferringDecoder(
                config = stringDeferringConfig,
                descriptor = serializer.descriptor,
                map = mapOf(
                    "x" to "1",
                    "y" to "fdsa",
                    "z" to "notnull",
                    "a.x" to "42",
                    "a.z" to "null",
                    "a.a.x" to "-1",
                    "b.0" to "1",
                    "c.0" to "key",
                    "c.1" to "1"
                )
            ).decodeSerializableValue(serializer)
        )
        assertEquals(
            basis,
            StringDeferringDecoder(
                config = stringDeferringConfig,
                descriptor = serializer.descriptor,
                map = mapOf(
                    "x" to "1",
                    "y" to "fdsa",
                    "z" to "notnull",
                    "a" to "true",
                    "a.x" to "42",
                    "a.z" to "null",
                    "a.a.x" to "-1",
                    "a.a.a" to "null",
                    "b.0" to "1",
                    "c.0" to "key",
                    "c.1" to "1"
                )
            ).decodeSerializableValue(serializer)
        )
        assertEquals(
            basis,
            StringDeferringDecoder(
                config = stringDeferringConfig,
                descriptor = serializer.descriptor,
                map = mapOf(
                    "x" to "1",
                    "y" to "fdsa",
                    "z" to "notnull",
                    "a" to "true",
                    "a.x" to "42",
                    "a.z" to "null",
                    "a.a.x" to "-1",
                    "a.a.a" to "null",
                    "a.a.a.x" to "1",
                    "b.0" to "1",
                    "c.0" to "key",
                    "c.1" to "1"
                )
            ).decodeSerializableValue(serializer)
        )
        assertEquals(
            basisButNull,
            StringDeferringDecoder(
                config = stringDeferringConfig,
                descriptor = serializer.descriptor,
                map = mapOf(
                    "x" to "1",
                    "y" to "fdsa",
                    "z" to "notnull",
                    "a" to "null",
//                    "a.x" to "42",
//                    "a.z" to "null",
//                    "a.a.x" to "-1",
//                    "a.a.a" to "false",
                    "b.0" to "1",
                    "c.0" to "key",
                    "c.1" to "1"
                )
            ).decodeSerializableValue(serializer).also { println(it) }
        )
        assertEquals(
            basis,
            StringDeferringDecoder(
                config = stringDeferringConfig,
                descriptor = serializer.descriptor,
                map = mapOf(
                    "x" to "1",
                    "y" to "fdsa",
                    "z" to "notnull",
                    "a" to "%{\"x\": 42, \"a\":{\"x\":-1}}",
                    "b" to "%[1]",
                    "c" to "%{\"key\":1}"
                )
            ).decodeSerializableValue(serializer)
        )
        assertEquals(
            basis,
            StringDeferringDecoder(
                config = stringDeferringConfig,
                descriptor = serializer.descriptor,
                map = mapOf(
                    "x" to "1",
                    "y" to "fdsa",
                    "z" to "notnull",
                    "a" to "{\"x\": 42, \"a\":{\"x\":-1}}",
                    "b" to "[1]",
                    "c" to "{\"key\":1}"
                )
            ).decodeSerializableValue(serializer)
        )
    }

    @Test
    fun csvHeaders() {
//        StringDeferringHeaderGenerator(ClientModule).apply {
//            decodeSerializableValue(TestObj.serializer())
//        }.headers.let { println(it) }
        StringDeferringEncoder(stringDeferringConfig, steadyHeaders = true).headers(TestObj.serializer().descriptor).let(::println)
    }

    @Test
    fun csvSequence() {
        val data = listOf(basis, TestObj())
        assertEquals(
            data,
            buildString { csv.encodeSequenceToAppendable(TestObj.serializer(), data.asSequence(), this) }
                .also { println(it) }
                .let { csv.decodeToSequence(it.iterator(), TestObj.serializer()) }
                .toList()
        )
    }

    @Test
    fun csvRootList() {
        val data = listOf(basis, TestObj())
        assertEquals(
            data,
            csv.encodeToString(ListSerializer(TestObj.serializer()), data)
                .also { println(it) }
                .let { csv.decodeFromString(ListSerializer(TestObj.serializer()), it) }
        )
    }

    fun <T> StringFormat.roundTripTest(serializer: KSerializer<T>, value: T) {
        assertEquals(
            value,
            decodeFromString(serializer, encodeToString(serializer, value).also { println(it) })
        )
    }

    @Test
    fun testFormats() {
        formats.forEach {
            it.roundTripTest(TestObj.serializer(), TestObj())
            it.roundTripTest(TestObj.serializer(), basis)
            it.roundTripTest(ListSerializer(TestObj.serializer()), listOf(basis, TestObj(), basis))
        }
    }

    inline fun <reified E: Exception> assertException(action: ()->Unit): E {
        try {
            action()
            fail("Did not throw")
        } catch(e: Exception) {
            if(e !is E) throw e
            return e
        }
    }

    @Test fun goodPrimitiveErrors() {
        assertException<SerializationException> {
            csv.decodeFromString(
                ListSerializer(TestObj.serializer()), """
            x,y,z,a,b,c
            42,Test,null,null,"%[1,2,3]","%{""a"":1}"
            asdf,Test,null,null,"%[1,2,3]","%{""a"":1}"
        """.trimIndent()
            )
        }.also {
            assertContains(it.message!!, "x", ignoreCase = true)
            assertContains(it.message!!, "record 2", ignoreCase = true)
        }
    }

    @Test fun goodDeferErrors() {
        assertException<SerializationException> {
            csv.decodeFromString(
                ListSerializer(TestObj.serializer()), """
            x,y,z,a,b,c
            42,Test,null,null,"%[1,2,3]","%{""a"":1}"
            21,Test,null,null,"%[[1,2,3]","%{""a"":1}"
        """.trimIndent()
            )
        }.also {
            it.printStackTrace()
            assertContains(it.message!!, "b", ignoreCase = true)
            assertContains(it.message!!, "record 2", ignoreCase = true)
        }
    }

    @Test fun missingEntriesParse() {
        """
        first,second,third
        1,2
        3,4,5
        6,7
        """.trimIndent().iterator().csvLines().asMaps().forEach {
            println(it)
        }
    }

    @Test fun parsetester() {
        """
        auctionDay,auction,venue,company,auctionDate,city,state,country,lotNumber,sortOrder,lotType,year,make,model,bodyStyle,trim,engine,provenance,interiorColor,exteriorColor,odometer.amount,odometer.unit,vin,externalImages.0,link,charity,custom,ignoreInPortfolios,reserve,estimatedHighPrice,estimatedHighPrice.without.currency,estimatedHighPrice.without.original,estimatedLowPrice,estimatedLowPrice.without.currency,estimatedLowPrice.without.original
        ad2f9001-bc43-4567-b897-8f408de80800,4cd834bd-5fe8-4401-8701-128af928b25f,e963ce68-d26c-4233-8f63-97931ed46cf0,f0ddceec-9b04-4f38-aa8c-5d2796bfd6d0,2024-10-28,Phoenix,AZ,US,240,240,652f3f63-101a-45d6-9c14-eb2ccc514729,,Porsche,356 B Coupe,,,,,N/A,Black,,,N/A,https://rmsothebys-cdn.azureedge.net/2/8/3/4/2/0/283420923ee3bd998acb17e4c0fd8eee6ec23c3f.jpg,https://rmsothebys.com/auctions/rk24/lots/r0209-porsche-356-b-coupe/,FALSE,FALSE,FALSE,FALSE,TRUE,USD,"6,000",TRUE,USD,3000
        """.trimIndent().iterator().csvLines().asMaps().forEach {
            println(it)
        }
    }

    @Serializable
    data class HasList(
        val x: List<Int> = listOf(),
        val y: String = "",
    )
    @Test fun hasList() {
        csv.roundTripTest(ListSerializer(HasList.serializer()), listOf(HasList(x = listOf(1, 2)), HasList(x = listOf(3, 4))))
    }
}
