# KotlinX Serialization CSV Durable

[![Maven Central Version](https://img.shields.io/maven-central/v/com.lightningkite/kotlinx-serialization-csv-durable)](https://central.sonatype.com/artifact/com.lightningkite/kotlinx-serialization-csv-durable)
[![Nightly](https://img.shields.io/maven-metadata/v?strategy=latestProperty&label=lightningkite-maven-nightly&metadataUrl=https://lightningkite-maven.s3.us-west-2.amazonaws.com/com/lightningkite/kotlinx-serialization-csv-durable/maven-metadata.xml
)](https://lightningkite-maven.s3.us-west-2.amazonaws.com/com/lightningkite/kotlinx-serialization-csv-durable/maven-metadata.xml)
[![License](https://img.shields.io/github/license/lightningkite/kotlinx-serialization-csv-durable?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0)
[![CI Status](https://img.shields.io/github/actions/workflow/status/lightningkite/kotlinx-serialization-csv-durable/publishInternal.yml)](https://github.com/lightningkite/kotlinx-serialization-csv-durable/publishInternal.yml)
[![KDoc](https://img.shields.io/badge/docs-kdoc-blue)](https://lightningkite-maven.s3.us-west-2.amazonaws.com/com/lightningkite/kotlinx-serialization-csv-durable/docs/index.html)

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?logo=kotlin&label=2.2.0)
![Android](https://img.shields.io/badge/platform-android-blue)
![JVM](https://img.shields.io/badge/platform-jvm-blue)
![JS](https://img.shields.io/badge/platform-js-blue)
![iOS](https://img.shields.io/badge/platform-ios-blue)
![Mac](https://img.shields.io/badge/platform-mac-blue)

Maintained by

<svg id="LK_Logo_Gold_" data-name="LK Logo (Gold)" xmlns="http://www.w3.org/2000/svg" width="225" height="40.099" viewBox="0 0 225 40.099">
<path id="Path_1" data-name="Path 1" d="M137.424,884.183l-5.163,14.345,9.318,1.5-5.786,10.554,12.965-13.669-10.392-1.464,2.661-7.177h15.4v18.722L123.045,924.02,160.8,910.467V884.183Z" transform="translate(-123.045 -883.921)" fill="#fcb912"></path>
<g id="Group_1" data-name="Group 1" transform="translate(51.196)">
<path id="Path_2" data-name="Path 2" d="M213.16,910.436V884.184h2.913v23.589h7.469v2.663Z" transform="translate(-213.16 -883.922)" fill="#fcb912"></path>
<path id="Path_3" data-name="Path 3" d="M239.606,884.184v26.252h-2.913V884.184Z" transform="translate(-223.324 -883.922)" fill="#fcb912"></path>
<path id="Path_4" data-name="Path 4" d="M259.828,890.324v2.25h-2.763v-2.4c0-2.363-.934-3.788-3.062-3.788-2.091,0-3.063,1.425-3.063,3.788V904.05c0,2.362.971,3.788,3.063,3.788,2.128,0,3.062-1.425,3.062-3.788V898.8h-2.689v-2.625h5.452V903.9c0,3.975-1.867,6.6-5.9,6.6-4,0-5.863-2.625-5.863-6.6V890.324c0-3.975,1.867-6.6,5.863-6.6C257.962,883.724,259.828,886.349,259.828,890.324Z" transform="translate(-228.235 -883.724)" fill="#fcb912"></path>
<path id="Path_5" data-name="Path 5" d="M277.93,898.623v11.813h-2.913V884.184h2.913V896h6.423V884.184h2.913v26.252h-2.913V898.623Z" transform="translate(-239.875 -883.922)" fill="#fcb912"></path>
<path id="Path_6" data-name="Path 6" d="M306.26,910.436V886.847h-4.818v-2.663H313.99v2.663h-4.818v23.589Z" transform="translate(-251.288 -883.922)" fill="#fcb912"></path>
<path id="Path_7" data-name="Path 7" d="M330.007,910.436h-2.614V884.184h3.7l6.05,18.977V884.184h2.577v26.252h-3.025l-6.684-21.227Z" transform="translate(-262.495 -883.922)" fill="#fcb912"></path>
<path id="Path_8" data-name="Path 8" d="M358.638,884.184v26.252h-2.913V884.184Z" transform="translate(-274.732 -883.922)" fill="#fcb912"></path>
<path id="Path_9" data-name="Path 9" d="M370.7,910.436h-2.615V884.184h3.7l6.05,18.977V884.184h2.577v26.252h-3.025L370.7,889.209Z" transform="translate(-280.071 -883.922)" fill="#fcb912"></path>
<path id="Path_10" data-name="Path 10" d="M408.127,890.324v2.25h-2.763v-2.4c0-2.363-.934-3.788-3.063-3.788-2.091,0-3.062,1.425-3.062,3.788V904.05c0,2.362.971,3.788,3.062,3.788,2.129,0,3.063-1.425,3.063-3.788V898.8h-2.69v-2.625h5.453V903.9c0,3.975-1.867,6.6-5.9,6.6-4,0-5.863-2.625-5.863-6.6V890.324c0-3.975,1.867-6.6,5.863-6.6C406.26,883.724,408.127,886.349,408.127,890.324Z" transform="translate(-292.283 -883.724)" fill="#fcb912"></path>
<path id="Path_11" data-name="Path 11" d="M440.329,899.035l-1.532,2.587v8.813h-2.913V884.184H438.8V896.86l6.946-12.676h2.95l-6.61,11.963,6.834,14.289h-2.988Z" transform="translate(-309.351 -883.922)" fill="#fcb912"></path>
<path id="Path_12" data-name="Path 12" d="M467.064,884.184v26.252h-2.913V884.184Z" transform="translate(-321.559 -883.922)" fill="#fcb912"></path>
<path id="Path_13" data-name="Path 13" d="M478.96,910.436V886.847h-4.818v-2.663H486.69v2.663h-4.817v23.589Z" transform="translate(-325.874 -883.922)" fill="#fcb912"></path>
<path id="Path_14" data-name="Path 14" d="M509.458,895.809v2.625h-6.386v9.338h7.842v2.663H500.159V884.184h10.756v2.663h-7.842v8.963Z" transform="translate(-337.11 -883.922)" fill="#fcb912"></path>
</g>
</svg>

Written in pure Kotlin common code, published for the platforms indicated above int he badges.

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
