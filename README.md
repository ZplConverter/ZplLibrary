# ZplLibrary

![NuGet version](https://registry.divios.org/api/badge/latest/releases/io/github/divios/ZplLibrary?color=40c14a&name=ZplLibrary)
[![GitHub license](https://img.shields.io/github/license/sungaila/PDFtoZPL?style=flat-square)](https://github.com/sungaila/PDFtoZPL/blob/master/LICENSE)

Simple java library to transform files to 
[Zebra Programming Language commands](https://en.wikipedia.org/wiki/Zebra_(programming_language)).
Supports java 11 and above.

This library is based on the .net repo [PDFtoZPL](https://github.com/sungaila/PDFtoZPL)

## Installation

To add a dependency on ZplLibrary using Maven, use the following:

``` xml
<repository>
  <id>divios-repo-releases</id>
  <name>Divios Repository</name>
  <url>https://registry.divios.org/releases</url>
</repository>

<dependency>
  <groupId>io.github.divios</groupId>
  <artifactId>ZplLibrary</artifactId>
  <version>{zpllibrary.version}</version>
</dependency>
```

To add a dependency using Gradle:

``` gradle
repositories {
  maven {
    url 'https://registry.divios.org/releases'
  }
}

dependencies {
  implementation 'io.github.divios:ZplLibrary:{zpllibrary.version}'
}
```

## How does it work?
0. Use ImageIO to render images to a BufferedImage
1. Make the BufferedImage monochrome
2. Convert the BufferedImage into a ^GF (Graphic Field) command
3. Optional: Compress the command hexdecimal data to shrink the ZPL code in size
4. Return the generated ZPL code

## Usage

Just call one of the following static methods:
* `ZPLConversion.convertPdf()`
* `ZPLConversion.convertBitmap()`

Additionally, you can use ConversionFacade and just pass the path or base64
representation of the file and let the library figure out the rest.
* `ConversionFacade.convertFileToZPL()`

## Print to Zebra printer

This library does not cover the printing part. We can recommend using the
[SDK](https://mvnrepository.com/artifact/com.zebra/zsdk-api) provided by Zebra Technologies.
