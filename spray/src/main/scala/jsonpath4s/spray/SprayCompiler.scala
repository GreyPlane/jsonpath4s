package jsonpath4s.spray

import spray.json.*
import spray.json.optics.*
import monocle.*
import monocle.function.Index
import jsonpath4s.optics.*

private val sprayOpticsPrimitive = Primitive[JsValue, Vector](
  Iso.id[JsValue],
  name => jsObject.andThen(jsObjectIndex.index(name)),
  jsArray,
  i => jsArray.andThen(Index.vectorIndex[JsValue].index(i)),
  jsDescendants,
  jsPlated.plate
)

given Compiler[JsValue] = Compiler(sprayOpticsPrimitive)
