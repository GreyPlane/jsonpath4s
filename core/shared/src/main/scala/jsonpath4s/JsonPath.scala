package jsonpath4s

enum Identifier {
  case Root
  case Current
}

enum Selector {
  case Name(name: String)
  case Index(i: Int)
  case Wildcard
  case Slice(start: Option[Int], end: Option[Int], step: Option[Int])
  case Filter(expr: String)
}

enum Segment {
  case Children(selectors: Seq[Selector])
  case Descendants(selectors: Seq[Selector])
}

final case class JsonPath(
    identifier: Identifier,
    segments: Seq[Segment]
)
