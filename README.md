# KotlinX Serialization CSV Durable

A serialization scheme for CSVs that is *durable*, meaning that it is meant to work for all cases (sublists, polymorphism) without failure.

To accomplish this, types that cannot be represented using CSVs (due to indeterminate column count) are encoded using a fallback string format (JSON by default).

In addition, it was set up to handle CSVs that are slightly non-standard - for example, it will work on `"\r\n"` line-terminated files just as well as `"\rn"` line-terminated files.

Supports:

- Sequence Reading
  - Complex fields (struct, polymorphic, list, etc.) that are escaped using a special character (default '%')
  - Structure fields by path (i.e. header is `dataclass.field`)
- Full List Reading
    - Complex fields (struct, polymorphic, list, etc.) that are escaped using a special character (default '%')
    - List fields by index (i.e. header is `list.0`)
    - List fields by escape (i.e. header is `list`, value is `%[1, 2, 3]`)
    - Structure fields by path (i.e. header is `dataclass.field`)
- Sequence Writing
  - Will write using steady headers - it will separate fields into multiple columns as long as there is a stable number of columns 
- Full List Reading
  - Will write using part headers - it will separate fields into multiple columns, since it can calculate the number of needed columns up front.

```kotlin
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
```
