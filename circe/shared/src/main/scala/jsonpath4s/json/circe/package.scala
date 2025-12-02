package jsonpath4s.json

import cats.PartialOrder
import cats.syntax.apply.given
import cats.syntax.eq.given
import io.circe.Json as CirceJson
import io.circe.optics.all as circeOptics
import jsonpath4s.optics.{Compiler, Json, JsonOptics}
import monocle.*
import monocle.function.Index

package object circe {

  private given PartialOrder[CirceJson] = PartialOrder.from[CirceJson] {
    case (x, y) if x === y => 0
    case (x, y) =>
      x.asNumber
        .map2(y.asNumber) { case (l, r) =>
          l.toBigDecimal
            .map2(r.toBigDecimal) {
              case (ll, rr) if ll < rr => -1
              case _                   => Double.NaN
            }
            .getOrElse(Double.NaN)
        }
        .orElse {
          x.asString.map2(y.asString) { case (l, r) =>
            l.compareTo(r).toDouble
          }
        }
        .getOrElse(Double.NaN)
  }

  private given Json[CirceJson] = new Json[CirceJson] {
    def num(n: BigDecimal): CirceJson = CirceJson.fromBigDecimal(n)

    def str(str: String): CirceJson = CirceJson.fromString(str)

    def bool(bool: Boolean): CirceJson = CirceJson.fromBoolean(bool)

    def arr(arr: Vector[CirceJson]): CirceJson = CirceJson.arr(arr*)

    def obj(fields: Map[String, CirceJson]): CirceJson = CirceJson.obj(fields.toSeq*)

    def nul: CirceJson = CirceJson.Null
  }

  private given JsonOptics[CirceJson] = new JsonOptics[CirceJson] {
    def json: Iso[CirceJson, CirceJson] = Iso.id[CirceJson]

    def jsonBoolean: Prism[CirceJson, Boolean] = circeOptics.jsonBoolean

    def jsonObjectIndex: String => Optional[CirceJson, CirceJson] = fieldName => circeOptics.jsonObject.andThen(circeOptics.jsonObjectIndex.index(fieldName))

    def jsonArray: Prism[CirceJson, Vector[CirceJson]] = circeOptics.jsonArray

    def jsonArrayIndex: Int => Optional[CirceJson, CirceJson] = i => circeOptics.jsonArray.andThen(Index.vectorIndex[CirceJson].index(i))

    def jsonDescendants: Traversal[CirceJson, CirceJson] = circeOptics.jsonDescendants

    def jsonPlate: Traversal[CirceJson, CirceJson] = circeOptics.jsonPlated.plate
  }

  given Compiler[CirceJson] = Compiler[CirceJson]

}
