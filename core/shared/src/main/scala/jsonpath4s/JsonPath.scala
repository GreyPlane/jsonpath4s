package jsonpath4s

enum Identifier {
  case Root
  case Current
}

enum ValType {
  case Logical
  case Val
  case NodeLists
}

enum Value {
  case Num(v: BigDecimal)
  case Str(v: String)
  case Dynamic(singularQuery: Query)
  case True
  case False
  case Null
}

enum UnaryOperator {
  case Not
}

enum BinaryOperator {
  case Eq
  case NotEq
  case Less
  case LessEq
  case Greater
  case GreaterEq
  case And
  case Or
}

enum Query {
  case Relative(segments: Seq[Segment])
  case Absolute(jsonPath: JsonPath)
}

enum Expr {
  case Val(value: Value)
  case Apply(func: String, args: Seq[Expr])
  case BinOp(lhs: Expr, op: BinaryOperator, rhs: Expr)
  case UnaryOp(op: UnaryOperator, expr: Expr)
  case Exist(query: Query)
}

enum Selector {
  case Name(name: String)
  case Index(i: Int)
  case Wildcard
  case Slice(start: Option[Int], end: Option[Int], step: Option[Int])
  case Filter(expr: Expr)
}

enum Segment {
  case Children(selectors: Seq[Selector])
  case Descendants(selectors: Seq[Selector])
}

final case class JsonPath(
    identifier: Identifier,
    segments: Seq[Segment]
)
