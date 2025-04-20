package jsonpath4s.json.spray

import cats.PartialOrder
import cats.syntax.order.given
import jsonpath4s.optics.*
import monocle.*
import monocle.function.Index
import spray.json.*
import spray.json.optics.*

// avoid naming clash with spray JSON
private trait SprayCompiler {

  given PartialOrder[JsValue] = PartialOrder.from[JsValue] { (x, y) =>
    if (x == y) 0
    else
      (x, y) match {
        case (JsNumber(l), JsNumber(r)) => l.compare(r)
        case (JsString(l), JsString(r)) => l.compare(r)
        case _                          => Double.NaN
      }
  }

  private given Json[JsValue] = new Json[JsValue] {
    def num(n: BigDecimal): JsValue = JsNumber(n)

    def str(str: String): JsValue = JsString(str)

    def bool(bool: Boolean): JsValue = JsBoolean(bool)

    def arr(arr: Vector[JsValue]): JsValue = JsArray(arr)

    def obj(fields: Map[String, JsValue]): JsValue = JsObject(fields)

    def nul: JsValue = JsNull
  }

  private given JsonOptics[JsValue] = new JsonOptics[JsValue] {
    def json: Iso[JsValue, JsValue] = Iso.id[JsValue]

    def jsonBoolean: Prism[JsValue, Boolean] = jsBoolean

    def jsonObjectIndex: String => Optional[JsValue, JsValue] = name => jsObject.andThen(jsObjectIndex.index(name))

    def jsonArray: Prism[JsValue, Vector[JsValue]] = jsArray

    def jsonArrayIndex: Int => Optional[JsValue, JsValue] = i => jsArray.andThen(Index.vectorIndex[JsValue].index(i))

    def jsonDescendants: Traversal[JsValue, JsValue] = jsDescendants

    def jsonPlate: Traversal[JsValue, JsValue] = jsPlated.plate
  }

  given Compiler[JsValue] = Compiler[JsValue]

}
