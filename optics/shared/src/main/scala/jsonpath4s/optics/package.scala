package jsonpath4s

import monocle.Fold

package object optics {

  implicit class JsonPathExtension(val jsonPath: JsonPath) extends AnyVal {

    def compile[Json](using compiler: Compiler[Json]): Fold[Json, Json] = compiler.compile(jsonPath)

  }

}
