package jsonpath4s.json.spray

import jsonpath4s.JsonPathParser
import jsonpath4s.optics.*
import jsonpath4s.json.spray.given
import spray.json.*

class JsonPathSpraySuite extends munit.FunSuite {

  test("basic usage") {
    val json = {
      """
        |  {
        |     "o": {"j": 1, "k": 2},
        |     "a": [5, 3, [{"j": 4}, {"k": 6}]]
        |   }
        |""".stripMargin
    }.parseJson

    JsonPathParser
      .parse("""$..j""")
      .map(jsonPath =>
        val values                 = jsonPath.compile.getAll(json).toSet
        val expected: Set[JsValue] = Set(JsNumber(1), JsNumber(4))

        assertEquals(values, expected)
      )

  }

}
