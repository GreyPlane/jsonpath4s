package jsonpath4s.circe

import jsonpath4s.optics.{Compiler, Primitive}
import io.circe.*
import io.circe.optics.JsonOptics.*
import io.circe.optics.JsonObjectOptics.*
import jsonpath4s.*
import monocle.*
import monocle.function.Index

private val circeOpticsPrimitive = Primitive[Json, Vector](
  json = Iso.id[Json],
  jsonObjectIndex = name => jsonObject.andThen(jsonObjectIndex.index(name)),
  jsonArray = jsonArray,
  jsonArrayIndex = i => jsonArray.andThen(Index.vectorIndex[Json].index(i)),
  jsonDescendants = jsonDescendants,
  jsonPlate = jsonPlated.plate
)

given Compiler[Json] = Compiler(circeOpticsPrimitive)
