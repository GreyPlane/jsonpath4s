package jsonpath4s.optics.compiletime

import jsonpath4s.optics.Compiler
import jsonpath4s.{JsonPath, JsonPathError, JsonPathParser}
import monocle.Iso

import scala.quoted.*

object Macro extends Instance {
  private def __parse(input: String)(using Quotes): Expr[JsonPath] = {
    val ast = JsonPathParser.parse(input) match {
      case Left(JsonPathError.ParsingError(message)) => quotes.reflect.report.errorAndAbort(message)
      case Right(value)                              => value
    }
    Expr(ast)
  }

  private def _parse(inputExpr: Expr[String])(using Quotes): Expr[JsonPath] = {
    val input = inputExpr.valueOrAbort
    __parse(input)
  }

  transparent inline def parse(inline input: String): JsonPath = ${ _parse('input) }
}
