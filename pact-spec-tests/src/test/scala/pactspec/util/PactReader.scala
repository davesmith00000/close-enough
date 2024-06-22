package pactspec.util

import com.itv.scalapact.shared.json.IPactReader
import com.itv.scalapact.shared.matchir.IrNode
import io.circe.Decoder
import io.circe.parser._
import pactspec.util.PactImplicits
import com.itv.scalapact.circe14.JsonConversionFunctions

class PactReader extends IPactReader {
  import PactImplicits._

  def fromJSON(jsonString: String): Option[IrNode] =
    JsonConversionFunctions.fromJSON(jsonString)

  def jsonStringToScalaPact(json: String): Either[String, Pact] =
    readJson[Pact](json, "scala-pact pact")

  private def readJson[A: Decoder](json: String, dataType: String): Either[String, A] =
    parse(json).flatMap(_.as[A]) match {
      case Right(a) => Right(a)
      case Left(_)  => Left(s"Could not read $dataType from json: $json")
    }
}
