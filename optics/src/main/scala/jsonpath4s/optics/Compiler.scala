package jsonpath4s.optics

import cats.Traverse
import jsonpath4s.JsonPath
import monocle.*
import jsonpath4s.optics.function.*
import jsonpath4s.*

final case class Primitive[Json, T[_]: Traverse](
    json: Iso[Json, Json],
    jsonObjectIndex: String => Optional[Json, Json],
    jsonArray: Prism[Json, T[Json]],
    jsonArrayIndex: Int => Optional[Json, Json],
    jsonDescendants: Traversal[Json, Json],
    jsonPlate: Traversal[Json, Json]
)

@FunctionalInterface
trait Compiler[Json] {

  def compile(jsonPath: JsonPath): Fold[Json, Json] = combineAll(compileSegments(jsonPath.segments))

  def compileSelector(selector: Selector): Traversal[Json, Json]

  def compileSegments(segment: Seq[Segment]): Seq[Traversal[Json, Json]]

}

object Compiler {

  def apply[Json, T[_]: Traverse](primitive: Primitive[Json, T]): Compiler[Json] = {

    new Compiler[Json]:

      def compileSelector(selector: Selector): Traversal[Json, Json] = selector match
        case Selector.Name(name)              => primitive.jsonObjectIndex(name)
        case Selector.Index(i)                => primitive.jsonArrayIndex(i)
        case Selector.Wildcard                => primitive.jsonDescendants
        case Selector.Slice(start, end, step) => primitive.jsonArray.andThen(slice(start, end, step))
        case Selector.Filter(expr)            =>
          // TODO Implement me
          primitive.json

      def compileSegments(segments: Seq[Segment]): Seq[Traversal[Json, Json]] = segments.foldLeft[Seq[Traversal[Json, Json]]](Vector(primitive.json)) {
        case (optics, segment) =>
          segment match {
            case Segment.Children(selectors) =>
              val selectorOptics = selectors.map(compileSelector)
              optics.flatMap(optic => selectorOptics.map(optic.andThen))
            case Segment.Descendants(selectors) =>
              val selectorOptics = selectors.map(compileSelector)
              optics.flatMap(optic => selectorOptics.map(optic.andThen(cosmos(primitive.jsonPlate)).andThen))
          }
      }
  }

}
