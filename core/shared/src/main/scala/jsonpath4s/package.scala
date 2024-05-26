package object jsonpath4s {

  implicit class JsonPathLiteral(sc: StringContext) extends AnyVal {

    def jsonpath(args: Any*): JsonPath = {
      JsonPathParser.unsafeParse(sc.s(args*))
    }

  }

}
