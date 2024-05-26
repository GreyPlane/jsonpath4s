package jsonpath4s.json

import io.circe.*
import io.circe.optics.JsonObjectOptics.*
import io.circe.optics.JsonOptics.*
import jsonpath4s.optics.{Compiler, Primitive}
import monocle.*
import monocle.function.Index

package object circe {

  private val circeOpticsPrimitive = Primitive[Json, Vector](
    json = Iso.id[Json],
    jsonObjectIndex = name => jsonObject.andThen(jsonObjectIndex.index(name)),
    jsonArray = jsonArray,
    jsonArrayIndex = i => jsonArray.andThen(Index.vectorIndex[Json].index(i)),
    jsonDescendants = jsonDescendants,
    jsonPlate = jsonPlated.plate
  )

  given Compiler[Json] = Compiler(circeOpticsPrimitive)

}
