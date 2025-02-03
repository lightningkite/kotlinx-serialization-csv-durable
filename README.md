# KotlinX Serialization CSV Durable

A serialization scheme for CSVs that is *durable*, meaning that it is meant to work for all cases (sublists, polymorphism) without failure.

To accomplish this, types that cannot be represented using CSVs (due to indeterminate column count) are encoded using a fallback string format (JSON by default).

In addition, it was set up to handle CSVs that are slightly non-standard - for example, it will work on `"\r\n"` line-terminated files just as well as `"\rn"` line-terminated files.

It can also handle streaming many objects via `Sequence`.


```kotlin
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
```

<!-- test commit -->
