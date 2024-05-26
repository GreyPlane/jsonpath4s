# jsonpath4s

Compile [JsonPath](https://datatracker.ietf.org/doc/rfc9535/) to [Monocle](https://www.optics.dev/Monocle/) optics, pass
and use it freely.

## Roadmap

- Support filter and function extension.

## Usage

1. Add core dependency.

 ```scala
libraryDependencies ++= Seq(
  "io.github.greyplane" %% "jsonpath4s-core" % "version",
  "io.github.greyplane" %% "jsonpath4s-optics" % "version"
)
```

2. Add JSON library support as you needed, currently we supported `circe` and `spray-json`.

 ```scala
libraryDependencies ++= Seq(
  "io.github.greyplane" %% "jsonpath4s-circe" % "version",
  // or if you're using spray-json
  "io.github.greyplane" %% "jsonpath4s-spray-json" % "version"
)
```

3. If you're using Scala 2,
   here's
   the [extra config](https://docs.scala-lang.org/scala3/guides/migration/compatibility-classpath.html#a-scala-213-module-can-depend-on-a-scala-3-artifact)
   required for Scala 2 to use Scala 3 artifact.

 ```scala
 ThisBuild / scalacOptions ++= Seq("-Ytasty-reader")

// add cross config for every artifact
("io.github.greyplane" %% "jsonpath4s-core" % "version").cross(CrossVersion.for2_13Use3)
```

4. Add imports as needed, for typical usage the following should suffice.

 ```scala
import jsonpath4s._
import jsonpath4s.optics._
// if you're using spray-json, import jsonpath4s.json.spray._
import jsonpath4s.json.circe._
```

if you're using scala 3

```scala 3
import jsonpath4s.*
import jsonpath4s.optics.*
import jsonpath4s.json.circe.given
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

If you'd like use one JsonPath multiple times, you could just compile it and pass it around, after all, it's just a
normal optic!

```scala
// make sure you've import json support library
val a: Fold[Json, Json] = jsonpath"$$.a".compile
```