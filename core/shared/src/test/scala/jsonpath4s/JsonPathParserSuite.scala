package jsonpath4s

class JsonPathParserSuite extends munit.FunSuite {

  test("string interpolation") {
    val fieldName = "a"

    val result = jsonpath"$$.$fieldName"

    assertEquals(result, JsonPath(Identifier.Root, Seq(Segment.Children(Seq(Selector.Name("a"))))))
  }

  test("should parse dot syntax") {
    val result = JsonPathParser.parse("""$.a""")

    assertEquals(result, Right(JsonPath(Identifier.Root, Seq(Segment.Children(Seq(Selector.Name("a")))))))
  }

  test("should parse descendant syntax") {
    val result = JsonPathParser.parse("""$..a""")

    assertEquals(result, Right(JsonPath(Identifier.Root, Seq(Segment.Descendants(Seq(Selector.Name("a")))))))
  }

  test("should parse multiple dot selections") {
    val result = JsonPathParser.parse("""$.a.b.c""")

    assertEquals(
      result,
      Right(
        JsonPath(
          Identifier.Root,
          Seq(
            Segment.Children(Seq(Selector.Name("a"))),
            Segment.Children(Seq(Selector.Name("b"))),
            Segment.Children(Seq(Selector.Name("c")))
          )
        )
      )
    )
  }

  test("filter") {
    val result = JsonPathParser.parse("""$[?@.*]""")

    println(result)
  }
}
