package jsonpath4s

class JsonPathParserSuite extends munit.FunSuite {

  test("should parse dot syntax") {
    val result = JsonPathParser.quickRun("""$.a""")

    assertEquals(result, Right(JsonPath(Identifier.Root, Seq(Segment.Children(Seq(Selector.Name("a")))))))
  }

  test("should parse descendant syntax") {
    val result = JsonPathParser.quickRun("""$..a""")

    assertEquals(result, Right(JsonPath(Identifier.Root, Seq(Segment.Descendants(Seq(Selector.Name("a")))))))
  }

  test("should parse multiple dot selections") {
    val result = JsonPathParser.quickRun("""$.a.b.c""")

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

}
