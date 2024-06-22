package pactspec.util

import com.itv.scalapact.shared.json.IPactReader

import scala.annotation.tailrec
import pactspec.util.{InteractionRequest, Interaction, MatchingRule, InteractionResponse}
import com.itv.scalapact.shared.matchir._
import com.itv.scalapact.shared.matchir.PactPathParseResult._
import com.itv.scalapact.shared.matchir.IrNodeRule._

object InteractionMatchers {

  case class OutcomeAndInteraction(outcome: MatchOutcome, closestMatchingInteraction: Interaction)

  val RequestSubject: String  = "request"
  val ResponseSubject: String = "response"

  def renderOutcome(
      outcome: Option[OutcomeAndInteraction],
      renderedOriginal: String,
      subject: String
  ): Either[String, Interaction] =
    outcome match {
      case None =>
        Left("Entirely failed to match, something went horribly wrong.")

      case Some(OutcomeAndInteraction(MatchOutcomeSuccess, interaction)) =>
        Right(interaction)

      case Some(OutcomeAndInteraction(f @ MatchOutcomeFailed(_, _), i)) =>
        Left(
          s"""Failed to match $subject
             | ...original
             |$renderedOriginal
             | ...closest match was...
             |${if (subject == RequestSubject) i.request.renderAsString else i.response.renderAsString}
             | ...Differences
             |${f.renderDifferences}
             """.stripMargin
        )
    }

  def matchOrFindClosestRequest(strictMatching: Boolean, interactions: List[Interaction], received: InteractionRequest)(
      implicit pactReader: IPactReader
  ): Option[OutcomeAndInteraction] = {
    @tailrec
    def rec(
        strict: Boolean,
        remaining: List[Interaction],
        actual: InteractionRequest,
        fails: List[(MatchOutcomeFailed, Interaction)]
    ): Option[OutcomeAndInteraction] =
      remaining match {
        case Nil =>
          fails.sortBy(_._1.drift).headOption.map(f => OutcomeAndInteraction(f._1, f._2))

        case x :: xs =>
          matchSingleRequest(strict, x.request.matchingRules, x.request, actual) match {
            case success: MatchOutcomeSuccess.type =>
              Option(OutcomeAndInteraction(success, x))

            case failure: MatchOutcomeFailed =>
              rec(strict, xs, actual, (failure, x) :: fails)
          }
      }

    rec(strictMatching, interactions, received, Nil)
  }

  def matchRequest(strictMatching: Boolean, interactions: List[Interaction], received: InteractionRequest)(implicit
      pactReader: IPactReader
  ): Either[String, Interaction] =
    if (interactions.isEmpty) Left("No interactions to compare with.")
    else
      renderOutcome(
        matchOrFindClosestRequest(strictMatching, interactions, received),
        received.renderAsString,
        RequestSubject
      )

  def matchSingleRequest(
      strictMatching: Boolean,
      rules: Option[Map[String, MatchingRule]],
      expected: InteractionRequest,
      received: InteractionRequest
  )(implicit pactReader: IPactReader): MatchOutcome =
    fromPactRules(rules) match {
      case Left(e) =>
        MatchOutcomeFailed(e)

      case Right(r) if strictMatching =>
        BodyMatching.matchBodiesStrict(expected.headers, expected.body, received.body)(
          r,
          pactReader
        )

      case Right(r) =>
        BodyMatching.matchBodies(expected.headers, expected.body, received.body)(r, pactReader)
    }

  def matchOrFindClosestResponse(
      strictMatching: Boolean,
      interactions: List[Interaction],
      received: InteractionResponse
  )(implicit pactReader: IPactReader): Option[OutcomeAndInteraction] = {
    @tailrec
    def rec(
        strict: Boolean,
        remaining: List[Interaction],
        actual: InteractionResponse,
        fails: List[(MatchOutcomeFailed, Interaction)]
    ): Option[OutcomeAndInteraction] =
      remaining match {
        case Nil =>
          fails.sortBy(_._1.drift).headOption.map(f => OutcomeAndInteraction(f._1, f._2))

        case x :: xs =>
          matchSingleResponse(strict, x.response.matchingRules, x.response, actual) match {
            case success: MatchOutcomeSuccess.type =>
              Option(OutcomeAndInteraction(success, x))

            case failure: MatchOutcomeFailed =>
              rec(strict, xs, actual, (failure, x) :: fails)
          }
      }

    rec(strictMatching, interactions, received, Nil)
  }

  def matchResponse(strictMatching: Boolean, interactions: List[Interaction])(implicit
      pactReader: IPactReader
  ): InteractionResponse => Either[String, Interaction] =
    received =>
      if (interactions.isEmpty) Left("No interactions to compare with.")
      else
        renderOutcome(
          matchOrFindClosestResponse(strictMatching, interactions, received),
          received.renderAsString,
          ResponseSubject
        )

  def matchSingleResponse(
      strictMatching: Boolean,
      rules: Option[Map[String, MatchingRule]],
      expected: InteractionResponse,
      received: InteractionResponse
  )(implicit pactReader: IPactReader): MatchOutcome =
    fromPactRules(rules) match {
      case Left(e) =>
        MatchOutcomeFailed(e)

      case Right(r) if strictMatching =>
        BodyMatching.matchBodiesStrict(expected.headers, expected.body, received.body)(
          r,
          pactReader
        )

      case Right(r) =>
        BodyMatching.matchBodies(expected.headers, expected.body, received.body)(r, pactReader)
    }

  private def parsePathIntoRule(pair: (String, MatchingRule)): Either[String, IrNodeMatchingRules] =
    (IrNodePath.fromPactPath(pair._1), pair._2) match {
      case (e: PactPathParseFailure, _) =>
        Left(e.errorString)

      case (PactPathParseSuccess(path), MatchingRule(Some("type"), None, None)) =>
        Right(IrNodeMatchingRules(IrNodeTypeRule(path)))

      case (PactPathParseSuccess(path), MatchingRule(Some("type"), None, Some(len))) =>
        Right(
          IrNodeMatchingRules(IrNodeTypeRule(path)) + IrNodeMatchingRules(IrNodeMinArrayLengthRule(len, path))
        )

      case (PactPathParseSuccess(path), MatchingRule(Some("type"), Some(regex), Some(len))) =>
        Right(
          IrNodeMatchingRules(IrNodeTypeRule(path)) + IrNodeMatchingRules(
            IrNodeRegexRule(regex, path)
          ) + IrNodeMatchingRules(
            IrNodeMinArrayLengthRule(len, path)
          )
        )

      case (PactPathParseSuccess(path), MatchingRule(Some("regex"), Some(regex), None)) =>
        Right(IrNodeMatchingRules(IrNodeRegexRule(regex, path)))

      case (PactPathParseSuccess(path), MatchingRule(Some("regex"), Some(regex), Some(len))) =>
        Right(
          IrNodeMatchingRules(IrNodeRegexRule(regex, path)) + IrNodeMatchingRules(
            IrNodeMinArrayLengthRule(len, path)
          )
        )

      case (PactPathParseSuccess(path), MatchingRule(None, Some(regex), None)) =>
        Right(IrNodeMatchingRules(IrNodeRegexRule(regex, path)))

      case (PactPathParseSuccess(path), MatchingRule(None, Some(regex), Some(len))) =>
        Right(
          IrNodeMatchingRules(IrNodeRegexRule(regex, path)) + IrNodeMatchingRules(
            IrNodeMinArrayLengthRule(len, path)
          )
        )

      case (PactPathParseSuccess(path), MatchingRule(Some("min"), None, Some(len))) =>
        Right(IrNodeMatchingRules(IrNodeMinArrayLengthRule(len, path)))

      case (PactPathParseSuccess(path), MatchingRule(Some("min"), Some(regex), Some(len))) =>
        Right(
          IrNodeMatchingRules(IrNodeRegexRule(regex, path)) + IrNodeMatchingRules(
            IrNodeMinArrayLengthRule(len, path)
          )
        )

      case (PactPathParseSuccess(path), MatchingRule(None, None, Some(len))) =>
        Right(IrNodeMatchingRules(IrNodeMinArrayLengthRule(len, path)))

      case (p, r) =>
        Left(
          "Failed to read rule: " + r.renderAsString + s" for path '${p.toOption.map(_.renderAsString).getOrElse("")}'"
        )
    }

  def fromPactRules(rules: Option[Map[String, MatchingRule]]): Either[String, IrNodeMatchingRules] = {

    val l: List[Either[String, IrNodeMatchingRules]] = rules match {
      case None =>
        List(Right(IrNodeMatchingRules.empty))

      case Some(ruleMap) =>
        ruleMap.toList.map(parsePathIntoRule)
    }

    @tailrec
    def rec(
        remaining: List[Either[String, IrNodeMatchingRules]],
        errorAcc: List[String],
        rulesAcc: IrNodeMatchingRules
    ): Either[String, IrNodeMatchingRules] =
      remaining match {
        case Nil if errorAcc.nonEmpty =>
          Left("Pact rule conversion errors:\n" + errorAcc.mkString("\n"))

        case Nil =>
          Right(rulesAcc)

        case Right(x) :: xs =>
          rec(xs, errorAcc, rulesAcc + x)

        case Left(x) :: xs =>
          rec(xs, x :: errorAcc, rulesAcc)
      }

    rec(l, Nil, IrNodeMatchingRules.empty)
  }

}
