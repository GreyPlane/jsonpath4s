package jsonpath4s.optics.compiletime

import jsonpath4s.*

import scala.quoted.{Expr, Quotes, ToExpr}

private trait Instance {

  given ToExpr[Value] with {
    def apply(x: Value)(using Quotes): Expr[Value] = x match {
      case Value.Num(v)         => '{ Value.Num(${ Expr(v) }) }
      case Value.Str(v)         => '{ Value.Str(${ Expr(v) }) }
      case Value.Dynamic(query) => '{ Value.Dynamic(${ Expr(query) }) }
      case Value.True           => '{ Value.True }
      case Value.False          => '{ Value.False }
      case Value.Null           => '{ Value.Null }
    }
  }

  given ToExpr[UnaryOperator] with {
    def apply(x: UnaryOperator)(using Quotes): Expr[UnaryOperator] = x match {
      case UnaryOperator.Not => '{ UnaryOperator.Not }
    }
  }

  given ToExpr[BinaryOperator] with {
    def apply(x: BinaryOperator)(using Quotes): Expr[BinaryOperator] = x match {
      case BinaryOperator.Eq        => '{ BinaryOperator.Eq }
      case BinaryOperator.NotEq     => '{ BinaryOperator.NotEq }
      case BinaryOperator.Less      => '{ BinaryOperator.Less }
      case BinaryOperator.LessEq    => '{ BinaryOperator.LessEq }
      case BinaryOperator.Greater   => '{ BinaryOperator.Greater }
      case BinaryOperator.GreaterEq => '{ BinaryOperator.GreaterEq }
      case BinaryOperator.And       => '{ BinaryOperator.And }
      case BinaryOperator.Or        => '{ BinaryOperator.Or }
    }
  }

  given ToExpr[Query] with {
    def apply(x: Query)(using Quotes): Expr[Query] = x match {
      case Query.Relative(segments) => '{ Query.Relative(${ Expr(segments) }) }
      case Query.Absolute(jsonPath) => '{ Query.Absolute(${ Expr(jsonPath) }) }
    }
  }

  given ToExpr[jsonpath4s.Expr] with {
    def apply(x: jsonpath4s.Expr)(using Quotes): Expr[jsonpath4s.Expr] = x match {
      case jsonpath4s.Expr.Val(value)          => '{ jsonpath4s.Expr.Val(${ Expr(value) }) }
      case jsonpath4s.Expr.Apply(func, args)   => '{ jsonpath4s.Expr.Apply(${ Expr(func) }, ${ Expr(args) }) }
      case jsonpath4s.Expr.BinOp(lhs, op, rhs) => '{ jsonpath4s.Expr.BinOp(${ Expr(lhs) }, ${ Expr(op) }, ${ Expr(rhs) }) }
      case jsonpath4s.Expr.UnaryOp(op, expr)   => '{ jsonpath4s.Expr.UnaryOp(${ Expr(op) }, ${ Expr(expr) }) }
      case jsonpath4s.Expr.Exist(query)        => '{ jsonpath4s.Expr.Exist(${ Expr(query) }) }
    }
  }

  given ToExpr[Selector] with {
    def apply(x: Selector)(using Quotes): Expr[Selector] = x match {
      case Selector.Name(name)              => '{ Selector.Name(${ Expr(name) }) }
      case Selector.Index(i)                => '{ Selector.Index(${ Expr(i) }) }
      case Selector.Wildcard                => '{ Selector.Wildcard }
      case Selector.Slice(start, end, step) => '{ Selector.Slice(${ Expr(start) }, ${ Expr(end) }, ${ Expr(step) }) }
      case Selector.Filter(expr)            => '{ Selector.Filter(${ Expr(expr) }) }
    }
  }

  given ToExpr[Segment] with {
    def apply(x: Segment)(using Quotes): Expr[Segment] = x match {
      case Segment.Children(selectors)    => '{ Segment.Children(${ Expr(selectors) }) }
      case Segment.Descendants(selectors) => '{ Segment.Descendants(${ Expr(selectors) }) }
    }
  }

  given ToExpr[Identifier] with {
    def apply(x: Identifier)(using Quotes): Expr[Identifier] = x match {
      case Identifier.Root    => '{ Identifier.Root }
      case Identifier.Current => '{ Identifier.Current }
    }
  }

  given ToExpr[JsonPath] with {
    def apply(x: JsonPath)(using Quotes): Expr[JsonPath] = x match {
      case JsonPath(identifier, segments) =>
        '{
          JsonPath(
            ${ Expr(identifier) },
            ${ Expr(segments) }
          )
        }
    }
  }
}
