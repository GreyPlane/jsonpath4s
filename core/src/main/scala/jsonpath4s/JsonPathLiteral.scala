package jsonpath4s

extension (sc: StringContext) {

  def jsonpath(args: Any*): JsonPath = {
    JsonPathParser.unsafeParse(sc.s(args*))
  }

}
