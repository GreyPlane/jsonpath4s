package jsonpath4s.optics

import cats.PartialOrder
import cats.syntax.apply.given
import cats.syntax.partialOrder.given
import jsonpath4s.*
import jsonpath4s.optics.function.*
import monocle.*

trait Json[J] {
  def num(n: BigDecimal): J
  def str(str: String): J
  def bool(bool: Boolean): J
  def arr(arr: Vector[J]): J
  def obj(fields: Map[String, J]): J
  def nul: J
}

trait JsonOptics[J] {
  def json: Iso[J, J]
  def jsonBoolean: Prism[J, Boolean]
  def jsonObjectIndex: String => Optional[J, J]
  def jsonArray: Prism[J, Vector[J]]
  def jsonArrayIndex: Int => Optional[J, J]
  def jsonDescendants: Traversal[J, J]
  def jsonPlate: Traversal[J, J]
}

trait Compiler[J] {

  def compile(jsonPath: JsonPath): Fold[J, J] = combineAll(compileSegments(jsonPath.segments))

  def compileSelector(selector: Selector): Traversal[J, J]

  def compileSegments(segment: Seq[Segment]): Seq[Traversal[J, J]]

}

object Compiler {
  def apply[J: {PartialOrder, Json as json, JsonOptics as jsonOptics}]: Compiler[J] = {
    new Compiler[J]:
      def compileExpr(expr: Expr): J => J = expr match
        case Expr.Val(value) =>
          value match
            case Value.Num(v) => Function.const(json.num(v))
            case Value.Str(v) => Function.const(json.str(v))
            case Value.Dynamic(singularQuery) =>
              singularQuery match
                case Query.Relative(segments) =>
                  val selector = combineAll(compileSegments(segments))
                  // TODO FIXME it has to be singular
                  (j: J) => selector.getAll(j).headOption.getOrElse(json.nul)
                case Query.Absolute(_) =>
                  throw new UnsupportedOperationException("Absolute query in filter selector is not supported")
            case Value.True  => Function.const(json.bool(true))
            case Value.False => Function.const(json.bool(false))
            case Value.Null  => Function.const(json.nul)
        case Expr.Apply(func, args) =>
          // TODO
          throw new UnsupportedOperationException("Function extension is not implemented yet")
        case Expr.BinOp(lhs, op, rhs) => {
          val l = compileExpr(lhs)
          val r = compileExpr(rhs)
          val compare = op match {
            case BinaryOperator.Eq        => (json: J) => l(json) === r(json)
            case BinaryOperator.NotEq     => (json: J) => l(json) =!= r(json)
            case BinaryOperator.Less      => (json: J) => l(json) < r(json)
            case BinaryOperator.LessEq    => (json: J) => l(json) <= r(json)
            case BinaryOperator.Greater   => (json: J) => l(json) > r(json)
            case BinaryOperator.GreaterEq => (json: J) => l(json) >= r(json)
            case BinaryOperator.And => (json: J) => jsonOptics.jsonBoolean.getOption(l(json)).map2(jsonOptics.jsonBoolean.getOption(r(json)))(_ && _).getOrElse(false)
            case BinaryOperator.Or  => (json: J) => jsonOptics.jsonBoolean.getOption(l(json)).map2(jsonOptics.jsonBoolean.getOption(r(json)))(_ || _).getOrElse(false)
          }

          compare.andThen(json.bool)
        }
        case Expr.UnaryOp(op, expr) =>
          op match
            case jsonpath4s.UnaryOperator.Not =>
              val operand = compileExpr(expr)
              (j: J) => json.bool(jsonOptics.jsonBoolean.getOption(operand(j)).exists(!_))

        case Expr.Exist(query) =>
          query match
            case Query.Relative(segments) =>
              val selector = combineAll(compileSegments(segments))
              (j: J) => json.bool(selector.nonEmpty(j))
            case Query.Absolute(jsonPath) =>
              throw new UnsupportedOperationException("Absolute query in filter selector is not supported")

      def compileSelector(selector: Selector): Traversal[J, J] = selector match
        case Selector.Name(name)              => jsonOptics.jsonObjectIndex(name)
        case Selector.Index(i)                => jsonOptics.jsonArrayIndex(i)
        case Selector.Wildcard                => jsonOptics.jsonDescendants
        case Selector.Slice(start, end, step) => jsonOptics.jsonArray.andThen(slice(start, end, step))
        case Selector.Filter(expr) =>
          val filter = compileExpr(expr).andThen(jsonOptics.jsonBoolean.getOption).andThen(_.getOrElse(false))
          jsonOptics.jsonDescendants.andThen(Optional.filter(filter))

      def compileSegments(segments: Seq[Segment]): Seq[Traversal[J, J]] = segments.foldLeft[Seq[Traversal[J, J]]](Vector(jsonOptics.json)) { case (optics, segment) =>
        segment match {
          case Segment.Children(selectors) =>
            val selectorOptics = selectors.map(compileSelector)
            optics.flatMap(optic => selectorOptics.map(optic.andThen))
          case Segment.Descendants(selectors) =>
            val selectorOptics = selectors.map(compileSelector)
            optics.flatMap(optic => selectorOptics.map(optic.andThen(cosmos(jsonOptics.jsonPlate)).andThen))
        }
      }
  }

}
