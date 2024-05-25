package jsonpath4s.circe

import jsonpath4s.optics.{Compiler, Primitive}
import io.circe.*
import io.circe.optics.JsonOptics.*
import io.circe.optics.JsonObjectOptics.*
import jsonpath4s.*
import monocle.*
import monocle.function.Index

object CirceCompiler {

  private val circeOpticsPrimitive = Primitive[Json, Vector](
    json = Iso.id[Json],
    jsonObjectIndex = name => jsonObject.andThen(jsonObjectIndex.index(name)),
    jsonArray = jsonArray,
    jsonArrayIndex = i => jsonArray.andThen(Index.vectorIndex[Json].index(i)),
    jsonDescendants = jsonDescendants,
    jsonPlate = jsonPlated.plate
  )

  val compiler: Compiler[Json, Vector] = Compiler(circeOpticsPrimitive)

  def apply(jsonPath: JsonPath): Fold[Json, Json] = compiler.compile(jsonPath)

}
