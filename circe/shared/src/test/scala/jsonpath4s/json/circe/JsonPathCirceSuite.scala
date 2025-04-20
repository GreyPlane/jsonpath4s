package jsonpath4s.json.circe

import cats.implicits.given
import io.circe.Json
import io.circe.optics.all.*
import io.circe.parser.*
import jsonpath4s.json.circe
import jsonpath4s.json.circe.given
import jsonpath4s.optics.*
import jsonpath4s.{JsonPath, JsonPathParser}
import monocle.function.Plated
import munit.Location

class JsonPathCirceSuite extends munit.FunSuite {

  private def assertCompileResult(expect: Set[Json])(json: Json, jsonPath: JsonPath)(implicit loc: Location): Unit = {
    val values = jsonPath.compile.getAll(json)

    assertEquals(values.toSet, expect)
  }

  private def assertCompileResultDup(expect: List[Json])(json: Json, jsonPath: JsonPath)(implicit loc: Location): Unit = {
    val values = jsonPath.compile.getAll(json).groupBy(identity)

    assertEquals(values, expect.groupBy(identity))
  }

  private def inParseResult(json: String, jsonPath: String)(f: (Json, JsonPath) => Unit) = {
    val path   = JsonPathParser.parse(jsonPath)
    val result = parse(json).map2(path)(f)
    assert(result.isRight)
  }

  test("child segment") {
    val json = """["a", "b", "c", "d", "e", "f", "g"]"""

    inParseResult(json, """$[0,3]""")(assertCompileResult(Set(Json.fromString("a"), Json.fromString("d"))))

    inParseResult(json, """$[0:2:5]""")(assertCompileResult(Set(Json.fromString("a"), Json.fromString("b"), Json.fromString("f"))))

    inParseResult(json, """$[0,0]""")(assertCompileResultDup(List(Json.fromString("a"), Json.fromString("a"))))
  }

  test("descendents segment") {
    val json = {
      """
        |  {
        |     "o": {"j": 1, "k": 2},
        |     "a": [5, 3, [{"j": 4}, {"k": 6}]]
        |   }
        |""".stripMargin
    }

    inParseResult(json, """$..j""")(assertCompileResult(Set(Json.fromInt(1), Json.fromInt(4))))

    inParseResult(json, """$..[0]""")(assertCompileResult(Set(Json.fromInt(5), Json.obj("j" -> Json.fromInt(4)))))

    inParseResult(json, """$..*""") { (json, jsonPath) =>
      val everythingWithoutItself = Plated.universe[Json](json).filterNot(_ == json).toSet
      assertCompileResult(everythingWithoutItself)(json, jsonPath)
    }

    inParseResult(json, """$..o""")(assertCompileResult(Set(Json.obj("j" -> Json.fromInt(1), "k" -> Json.fromInt(2)))))

    inParseResult(json, """$.o..[*,*]""")(assertCompileResultDup(List(Json.fromInt(1), Json.fromInt(2), Json.fromInt(1), Json.fromInt(2))))

    inParseResult(json, """$.a..[0,1]""")(assertCompileResult(Set(Json.fromInt(5), Json.fromInt(3), Json.obj("j" -> Json.fromInt(4)), Json.obj("k" -> Json.fromInt(6)))))
  }

  test("slice selector") {
    val json = """["a", "b", "c", "d", "e", "f", "g"]"""

    inParseResult(json, """$[1:3]""")(assertCompileResult(Set(Json.fromString("b"), Json.fromString("c"))))

    inParseResult(json, """$[5:]""")(assertCompileResult(Set(Json.fromString("f"), Json.fromString("g"))))

    inParseResult(json, """$[1:5:2]""")(assertCompileResult(Set(Json.fromString("b"), Json.fromString("d"))))

    inParseResult(json, """$[5:1:-2]""")(assertCompileResult(Set(Json.fromString("f"), Json.fromString("d"))))

    inParseResult(json, """$[::-1]""")(
      assertCompileResult(
        Set(Json.fromString("g"), Json.fromString("f"), Json.fromString("e"), Json.fromString("d"), Json.fromString("c"), Json.fromString("b"), Json.fromString("a"))
      )
    )
  }

  test("filter selector") {
    val json = """{
                 |  "a": [3, 5, 1, 2, 4, 6,
                 |        {"b": "j"},
                 |        {"b": "k"},
                 |        {"b": {}},
                 |        {"b": "kilo"}
                 |       ],
                 |  "o": {"p": 1, "q": 2, "r": 3, "s": 5, "t": {"u": 6}},
                 |  "e": "f"
                 |}""".stripMargin

    inParseResult(json, """$.a[?@.b]""") { case (j, path) =>
      val result = path.compile.getAll(j)
      println(result)
    }
  }
}
