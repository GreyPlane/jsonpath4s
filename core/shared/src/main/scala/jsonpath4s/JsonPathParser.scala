package jsonpath4s

import jsonpath4s.BinaryOperator.{And, Or}
import org.parboiled2.*
import org.parboiled2.support.hlist.*

import scala.annotation.unused

class JsonPathParser(val input: ParserInput) extends Parser {

  private val slash                    = '/'
  private val backslash                = '\\'
  private val singleQuote              = '\''
  private val doubleQuote              = '\"'
  @unused private val segmentSeparator = '.'
  private val sliceSeparator           = ':'

  private def root: Rule1[Identifier] = rule { '$' ~ push(Identifier.Root) }

  private def whitespaces: Rule0 = rule {
    zeroOrMore(ch(' '))
  }

  private def esc = rule {
    backslash
  }
  private def unescaped: Rule1[Char] = rule {
    CharPredicate.AlphaNum ~ push(lastChar)
  }
  private def escapable: Rule1[Char] = rule {
    (ch('\u0008') | '\u000C' | '\u000A' | '\u000D' | '\u0009' | slash | backslash) ~ push(lastChar)
  }
  private def singleQuoted: Rule1[Char] = rule {
    unescaped | (doubleQuote ~ push(doubleQuote)) | (esc ~ singleQuote) ~ push(singleQuote) | esc ~ escapable
  }
  private def doubleQuoted: Rule1[Char] = rule {
    unescaped | (singleQuote ~ push(singleQuote)) | (esc ~ doubleQuote) ~ push(doubleQuote) | esc ~ escapable
  }

  private def stringLiteral: Rule1[String] = rule {
    (singleQuote ~ oneOrMore(singleQuoted) ~> (_.mkString) ~ singleQuote) | (doubleQuote ~ oneOrMore(doubleQuoted) ~> (_.mkString) ~ doubleQuote)
  }
  private def nameSelector: Rule1[Selector] = rule {
    stringLiteral ~> Selector.Name.apply
  }

  private def wildcardSelector: Rule1[Selector] = rule {
    ch('*') ~ push(Selector.Wildcard)
  }

  private def digits: Rule1[String] = rule {
    capture(CharPredicate.Digit19 ~ zeroOrMore(CharPredicate.Digit))
  }

  private def negativeDigits: Rule1[String] = rule {
    (ch('-') ~ digits) ~> (_.prepended('-'))
  }

  private def int: Rule1[Int] = rule {
    (capture(str("0")) | digits | negativeDigits) ~> (_.toInt)
  }
  private def indexSelector: Rule1[Selector] = rule {
    int ~> Selector.Index.apply
  }

  private def sliceSelector: Rule1[Selector] = rule {
    (optional(int).named("slice selector start") ~ sliceSeparator ~ optional(int).named("slice selector end") ~ optional(sliceSeparator ~ int).named("slice select step"))
      ~> Selector.Slice.apply
  }

  private def not: Rule1[UnaryOperator] = rule {
    ch('!') ~ push(UnaryOperator.Not)
  }

  private def reduceNot(maybeOp: Option[UnaryOperator], expr: Expr) = maybeOp.fold(expr)(op => Expr.UnaryOp(op, expr))

  private def parenExpr: Rule1[Expr] = rule {
    (optional(not) ~ '(' ~ logicalExpr ~ ')') ~> reduceNot
  }

  private def reduceAnd(exprs: Seq[Expr]) = exprs.reduce { (e1, e2) => Expr.BinOp(e1, And, e2) }

  private def and: Rule1[Expr] = rule {
    ((whitespaces ~ basicExpr) + (whitespaces ~ str("&&"))) ~> reduceAnd
  }

  private def reduceOr(exprs: Seq[Expr]) = exprs.reduce { (e1, e2) => Expr.BinOp(e1, Or, e2) }

  private def or: Rule1[Expr] = rule {
    ((whitespaces ~ and) + (whitespaces ~ str("||"))) ~> reduceOr
  }

  private def filterQuery: Rule1[Expr] = rule {
    (relQuery | absQuery) ~> Expr.Exist.apply
  }

  private def absQuery: Rule1[Query] = rule {
    jsonPath ~> Query.Absolute.apply
  }

  private def relQuery: Rule1[Query] = rule {
    (ch('@') ~ zeroOrMore(segment)) ~> Query.Relative.apply
  }

  private def testExpr: Rule1[Expr] = rule {
    (optional(not) ~ whitespaces ~ (filterQuery | functionExpr)) ~> reduceNot
  }

  private def frac: Rule1[String] = rule {
    capture(ch('.') ~ oneOrMore(CharPredicate.Digit))
  }

  private def exp: Rule1[String] = rule {
    capture(ch('e') ~ optional(ch('-') | ch('+')) ~ oneOrMore(CharPredicate.Digit))
  }

  private def reduceNumber(integer: String, maybeFrac: Option[String], maybeExp: Option[String]) = {
    BigDecimal(integer ++ maybeFrac.getOrElse("") ++ maybeExp.getOrElse(""))
  }

  private def number: Rule1[Value] = rule {
    (digits | (str("-0") ~ push("0"))) ~ optional(frac) ~ optional(exp) ~> reduceNumber ~> Value.Num.apply
  }

  private def literal: Rule1[Expr] = rule {
    (number | (stringLiteral ~> Value.Str.apply) | (str("true") ~ push(Value.True)) | (str("false") ~ push(Value.False)) | (str("null") ~ push(
      Value.Null
    ))) ~> Expr.Val.apply
  }

  private def nameSegment: Rule1[Segment] = rule {
    (ch('[') ~ nameSelector ~ ']' | ch('.') ~ memberNameShorthandNameSelector) ~> { selector => Segment.Children(Seq(selector)) }
  }

  private def indexSegment: Rule1[Segment] = rule {
    (ch('[') ~ indexSelector ~ ']') ~> { selector => Segment.Children(Seq(selector)) }
  }

  private def singularQuerySegments: Rule1[Seq[Segment]] = rule {
    zeroOrMore(whitespaces ~ (nameSegment | indexSegment))
  }

  private def relSingularQuery: Rule1[Query] = rule {
    (ch('@') ~ singularQuerySegments) ~> Query.Relative.apply
  }

  private def absSingularQuery: Rule1[Query] = rule {
    (ch('$') ~ singularQuerySegments) ~> { segments => Query.Absolute(JsonPath(Identifier.Root, segments)) }
  }

  private def singularQuery: Rule1[Query] = rule {
    relSingularQuery | absSingularQuery
  }

  private def comparable: Rule1[Expr] = rule {
    literal | (singularQuery ~> { query => Expr.Val(Value.Dynamic(query)) }) | functionExpr
  }

  private def comparisonOps: Rule1[BinaryOperator] = rule {
    (str("==") ~ push(BinaryOperator.Eq)) | (str("!=") ~ push(BinaryOperator.NotEq)) | (str("<=") ~ push(BinaryOperator.LessEq))
      | (str(">=") ~ push(BinaryOperator.GreaterEq)) | (str("<") ~ push(BinaryOperator.Less)) | (str(">") ~ push(BinaryOperator.Greater))
  }

  private def comparisonExpr: Rule1[Expr] = rule {
    (comparable ~ whitespaces ~ comparisonOps ~ whitespaces ~ comparable) ~> Expr.BinOp.apply
  }

  private def functionName: Rule1[String] = rule {
    capture(CharPredicate.LowerAlpha | zeroOrMore(CharPredicate.LowerAlpha | '_' | CharPredicate.Digit))
  }

  private def functionArgument: Rule1[Expr] = rule {
    literal | filterQuery | functionExpr | logicalExpr
  }

  private def functionExpr: Rule1[Expr] = rule {
    (functionName ~ '(' ~ whitespaces ~ (functionArgument * (ch(',') ~ whitespaces)) ~ whitespaces ~ ')') ~> Expr.Apply.apply
  }

  private def basicExpr: Rule1[Expr] = rule {
    parenExpr | comparisonExpr | testExpr
  }

  private def logicalExpr: Rule1[Expr] = rule {
    or
  }

  private def filterSelector: Rule1[Selector] = rule {
    // TODO Implement me
    ('?' ~ whitespaces ~ logicalExpr) ~> Selector.Filter.apply
  }

  private def selector: Rule1[Selector] = rule {
    nameSelector.named("Name Selector") | wildcardSelector | sliceSelector.named("Slice") | indexSelector | filterSelector
  }

  private def nameFirst = rule {
    CharPredicate.Alpha ++ CharPredicate('_')
  }
  private def nameChar = rule {
    nameFirst | CharPredicate.Digit
  }
  private def memberNameShorthand: Rule1[String] = rule {
    capture(nameFirst ~ zeroOrMore(nameChar))
  }
  private def memberNameShorthandNameSelector: Rule1[Selector] = rule {
    memberNameShorthand ~> Selector.Name.apply
  }
  private def bracketedSelection: Rule1[Seq[Selector]] = rule {
    '[' ~ (selector + ',') ~ ']'
  }
  private def dotSelection: Rule1[Selector] = rule {
    ch('.') ~ (wildcardSelector | memberNameShorthandNameSelector)
  }

  private def childSegment: Rule1[Segment] = rule {
    bracketedSelection ~> Segment.Children.apply | dotSelection ~> (s => Segment.Children(Seq(s)))
  }
  private def descendantSegment = rule {
    str("..") ~ (
      bracketedSelection ~> Segment.Descendants.apply |
        (wildcardSelector | memberNameShorthandNameSelector) ~> (s => Segment.Descendants(Seq(s)))
    )
  }
  private def segment = rule {
    childSegment | descendantSegment
  }

  def jsonPath: Rule1[JsonPath] = rule {
    (root ~ zeroOrMore(segment) ~ EOI) ~> JsonPath.apply
  }

}

object JsonPathParser {

  import Parser.DeliveryScheme.Either

  def parse(input: String): Either[JsonPathError, JsonPath] = {
    val parser = new JsonPathParser(input)

    parser.jsonPath.run().left.map(err => JsonPathError.ParsingError(parser.formatError(err)))
  }

  def unsafeParse(input: String): JsonPath = {
    val parser = new JsonPathParser(input)

    parser.jsonPath.run().fold(err => throw new RuntimeException(parser.formatError(err)), identity)
  }
}
