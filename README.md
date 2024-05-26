# jsonpath4s

Compile [JsonPath](https://datatracker.ietf.org/doc/rfc9535/) to [Monocle](https://www.optics.dev/Monocle/) optics, pass
and use it freely.

## Roadmap

- Support filter and function extension

## Usage

1. Add core dependency

```scala
libraryDependencies ++= Seq(
  "io.github.greyplane" %% "jsonpath4s-core" % "version",
  "io.github.greyplane" %% "jsonpath4s-optics" % "version"
)
```

2. Add JSON library support as you needed, currently we supported `circe` and `spray-json`

for circe

```scala
libraryDependencies ++= Seq(
  "io.github.greyplane" %% "jsonpath4s-circe" % "version"
)
```

for spray-json

```scala
libraryDependencies ++= Seq(
  "io.github.greyplane" %% "jsonpath4s-spray-json" % "version"
)
```

3. Add imports as needed, for typical usage the following should suffice

```scala
import jsonpath4s._
import jsonpath4s.optics._
// if you're using spray-json, import jsonpath4s.spray._
import jsonpath4s.circe._
```

if you're using scala 3

```scala 3
import jsonpath4s.*
import jsonpath4s.optics.*
import jsonpath4s.circe.given
```

## Introduction

For simplest usage you could just utilizing the string interpolation functionality.

```scala
// This would throw RuntimeException if parsing failed!
val nodeLists: List[Json] = jsonpath"$$.a".compile.getAll(json)
```

For safer option, you should use `JsonPathParser` directly.

```scala
val maybeJsonPath: Either[JsonPathError, JsonPath] = JsonPathParser.parse("""$.a""")
```

If you'd like use one JsonPath multiple times, you could just compile it and pass it around, after all, it's just a normal optic!

```scala
// make sure you've import json support library
val a: Fold[Json, Json] = jsonpath"$$.a".compile
```