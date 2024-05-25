package jsonpath4s.spray

import jsonpath4s.JsonPathParser
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
      .quickRun("""$..j""")
      .map(jsonPath =>
        val values                 = SprayCompiler(jsonPath).getAll(json).toSet
        val expected: Set[JsValue] = Set(JsNumber(1), JsNumber(4))

        assertEquals(values, expected)
      )

  }

}
