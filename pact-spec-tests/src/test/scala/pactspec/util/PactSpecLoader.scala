package pactspec.util

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._
import com.itv.scalapact.shared.utils.PactLogger
import com.itv.scalapact.shared.{InteractionRequest, InteractionResponse}

import scala.io.Source

object PactSpecLoader {

  import PactImplicits._

  implicit lazy val RequestSpecCodecJson: Codec[RequestSpec]   = deriveCodec
  implicit lazy val ResponseSpecCodecJson: Codec[ResponseSpec] = deriveCodec

  def fromResource(version: String, path: String): String = {
    // PactLogger.message("Loading spec: " + s"/pact-specification-version-$version/testcases$path")
    val source = Source
      .fromURL(getClass.getResource(s"/pact-specification-version-$version/testcases$path"))
    val res = source.getLines().mkString("\n")
    source.close()
    res
  }

  def deserializeRequestSpec(json: String): Option[RequestSpec] =
    SpecReader.jsonStringToRequestSpec(json) match {
      case Right(r) => Option(r)
      case Left(l) =>
        PactLogger.error(s"Error reading json: $l\n$json")
        None
    }

  def deserializeResponseSpec(json: String): Option[ResponseSpec] =
    SpecReader.jsonStringToResponseSpec(json) match {
      case Right(r) => Option(r)
      case Left(l) =>
        PactLogger.error(s"Error reading json: $l\n$json")
        None
    }

}

case class RequestSpec(`match`: Boolean, comment: String, expected: InteractionRequest, actual: InteractionRequest)
case class ResponseSpec(`match`: Boolean, comment: String, expected: InteractionResponse, actual: InteractionResponse)

object SpecReader {

  implicit def tupleToInteractionRequest(pair: (InteractionRequest, Option[String])): InteractionRequest =
    pair._1.copy(body = pair._2)
  implicit def tupleToInteractionResponse(pair: (InteractionResponse, Option[String])): InteractionResponse =
    pair._1.copy(body = pair._2)

  type BrokenPact[I] = (Boolean, String, (I, Option[String]), (I, Option[String]))

  def jsonStringToSpec[I](json: String)(implicit decoder: Decoder[I]): Either[String, BrokenPact[I]] =
    for {
      matches  <- JsonBodySpecialCaseHelper.extractMatches(json)
      comment  <- JsonBodySpecialCaseHelper.extractComment(json)
      expected <- JsonBodySpecialCaseHelper.extractInteractionRequestOrResponse[I]("expected", json, decoder)
      actual   <- JsonBodySpecialCaseHelper.extractInteractionRequestOrResponse[I]("actual", json, decoder)
    } yield (matches, comment, expected, actual)

  val jsonStringToRequestSpec: String => Either[String, RequestSpec] = json =>
    jsonStringToSpec[InteractionRequest](json)(PactImplicits.interactionRequestDecoder) match {
      case Right(bp) =>
        Right(RequestSpec(bp._1, bp._2, bp._3, bp._4))

      case Left(s) =>
        Left(s)
    }

  val jsonStringToResponseSpec: String => Either[String, ResponseSpec] = json =>
    jsonStringToSpec[InteractionResponse](json)(PactImplicits.interactionResponseDecoder) match {
      case Right(bp) =>
        Right(ResponseSpec(bp._1, bp._2, bp._3, bp._4))

      case Left(s) =>
        Left(s)
    }

}

object JsonBodySpecialCaseHelper {

  val extractMatches: String => Either[String, Boolean] = json =>
    parse(json).map { j =>
      j.hcursor
        .downField("match")
        .focus
        .flatMap(_.asBoolean)
        .getOrElse(false)
    } match {
      case Left(e) =>
        Left("Extracting 'match': " + e)

      case Right(value) =>
        Right(value)
    }

  val extractComment: String => Either[String, String] = json =>
    parse(json).map { j =>
      j.hcursor
        .downField("comment")
        .focus
        .flatMap(_.asString)
        .getOrElse("<missing comment>")
    } match {
      case Left(e) =>
        Left("Extracting 'comment': " + e)

      case Right(value) =>
        Right(value)
    }

  def extractInteractionRequestOrResponse[I](
      field: String,
      json: String,
      decoder: Decoder[I]
  ): Either[String, (I, Option[String])] =
    separateRequestResponseFromBody(field, json)
      .flatMap(RequestResponseAndBody.unapply) match {
      case Some((Some(requestResponseMinusBody), maybeBody)) =>
        requestResponseMinusBody
          .as[I](decoder)
          .map(i => (i, maybeBody)) match {
          case Left(e) =>
            Left("Extracting 'expected or actual': " + e)

          case Right(value) =>
            Right(value)
        }

      case Some((None, _)) =>
        val msg = s"Could not convert request to Json object: $json"
        PactLogger.error(msg)
        Left(msg)

      case None =>
        val msg = s"Problem splitting request from body in: $json"
        PactLogger.error(msg)
        Left(msg)
    }

  private def separateRequestResponseFromBody(field: String, json: String): Option[RequestResponseAndBody] =
    parse(json).toOption.flatMap(j => j.hcursor.downField(field).focus).map { requestField =>
      val minusBody = requestField.hcursor.downField("body").focus match {
        case ok @ Some(_) => ok
        case None         => Some(requestField) // There wasn't a body, but there was still a request.
      }

      val requestBody = requestField.hcursor.downField("body").focus.flatMap { p =>
        if (p.isString) p.asString else Option(p.toString)
      }

      RequestResponseAndBody(minusBody, requestBody)
    }

  final case class RequestResponseAndBody(requestMinusBody: Option[Json], body: Option[String])

}
