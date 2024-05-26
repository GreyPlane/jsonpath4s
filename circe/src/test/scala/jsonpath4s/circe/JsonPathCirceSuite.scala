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

  private def inParseResult(json: String, jsonPath: String)(f: (Json, JsonPath) => Unit) = {
    parse(json).map2(JsonPathParser.quickRun(jsonPath))(f)
  }

  test("children segment") {

    inParseResult("""{ "a": { "b" : 1 } }""", """$.a.b""")(assertCompileResult(Set(Json.fromInt(1))))

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

    inParseResult(json, """$.o..[*,*]""") { (json, jsonPath) =>
      println(jsonPath.compile.getAll(json))
    }
  }

}
