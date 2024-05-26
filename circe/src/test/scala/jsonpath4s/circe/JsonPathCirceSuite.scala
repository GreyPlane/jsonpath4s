package jsonpath4s.circe

import io.circe.Json
import jsonpath4s.{JsonPath, JsonPathParser}
import jsonpath4s.optics.*
import jsonpath4s.circe.given
import io.circe.parser.*
import munit.Location
import cats.implicits.given
import io.circe.optics.all.*
import monocle.function.Plated

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
    parse(json).map2(JsonPathParser.parse(jsonPath))(f)
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

}
