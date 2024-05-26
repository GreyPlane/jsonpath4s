package jsonpath4s

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

  private def root: Rule1[Identifier]            = rule { '$' ~ push(Identifier.Root) }
  @unused private def current: Rule1[Identifier] = rule { '@' ~ push(Identifier.Current) }

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

  private def int: Rule1[Int] = rule {
    capture(CharPredicate.Digit19 ~ zeroOrMore(CharPredicate.Digit)) ~> (_.mkString.toInt)
  }
  private def indexSelector: Rule1[Selector] = rule {
    (ch('0') ~ push(0) | int | '-' ~ int) ~> Selector.Index.apply
  }

  private def sliceSelector: Rule1[Selector] = rule {
    (optional(int).named("slice selector start") ~ sliceSeparator ~ optional(int).named("slice selector end") ~ sliceSeparator ~ optional(int).named("slice select step"))
      ~> Selector.Slice.apply
  }

  private def filterSelector: Rule1[Selector] = rule {
    // TODO Implement me
    ('?' ~ capture(CharPredicate.All)) ~> Selector.Filter.apply
  }

  private def selector: Rule1[Selector] = rule {
    nameSelector | wildcardSelector | sliceSelector | indexSelector | filterSelector
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
