package jsonpath4s.spray

import jsonpath4s.JsonPath
import spray.json.*
import spray.json.optics.*
import monocle.*
import monocle.function.Index
import jsonpath4s.optics.*

object SprayCompiler {

  private val sprayOpticsPrimitive = Primitive[JsValue, Vector](
    Iso.id[JsValue],
    name => jsObject.andThen(jsObjectIndex.index(name)),
    jsArray,
    i => jsArray.andThen(Index.vectorIndex[JsValue].index(i)),
    jsDescendants,
    jsPlated.plate
  )

  val compiler: Compiler[JsValue, Vector] = Compiler(sprayOpticsPrimitive)

  def apply(jsonPath: JsonPath): Fold[JsValue, JsValue] = compiler.compile(jsonPath)

}
