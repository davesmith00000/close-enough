package com.itv.scalapactcore.common

import com.itv.scalapact.json.pactReaderInstance
import com.itv.scalapact.shared.matchir.IrNodeMatchingRules
import com.itv.scalapact.shared.{MatchingRule, Pact}
import com.itv.scalapactcore.common.matching.BodyMatching._
import com.itv.scalapactcore.common.matching.InteractionMatchers.OutcomeAndInteraction
import com.itv.scalapactcore.common.matching.{InteractionMatchers, MatchOutcomeFailed, MatchOutcomeSuccess}

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class InteractionMatchersSpec extends AnyFunSpec with Matchers {

  implicit def toOption[A](thing: A): Option[A] = Option(thing)

  describe("Matching bodies") {

    it("should be able to match plain text bodies") {

      val expected = "hello there!"

      matchBodies(None, expected, expected)(IrNodeMatchingRules.empty, pactReaderInstance).isSuccess shouldEqual true
      matchBodies(None, expected, "Yo ho!")(IrNodeMatchingRules.empty, pactReaderInstance).isSuccess shouldEqual false

    }

    it("should be able to handle missing bodies and no expectation of bodies") {

      withClue("None expected, none received") {
        matchBodies(None, None, None)(IrNodeMatchingRules.empty, pactReaderInstance).isSuccess shouldEqual true
      }

      withClue("Some expected, none received") {
        matchBodies(None, Some("hello"), None)(
          IrNodeMatchingRules.empty,
          pactReaderInstance
        ).isSuccess shouldEqual false
      }

      // Forgiving about what we receive
      withClue("None expected, some received") {
        matchBodies(None, None, Some("hello"))(IrNodeMatchingRules.empty, pactReaderInstance).isSuccess shouldEqual true
      }
      withClue("Some expected, some received") {
        matchBodies(None, Some("hello"), Some("hello"))(
          IrNodeMatchingRules.empty,
          pactReaderInstance
        ).isSuccess shouldEqual true
      }

    }

    it("should be able to match json bodies") {

      val expected =
        """
          |{
          |  "id":1234,
          |  "name":"joe",
          |  "hobbies": [
          |    "skiing",
          |    "fishing", "golf"
          |  ]
          |}
        """.stripMargin

      val received =
        """
          |{
          |  "id":1234,
          |  "name":"joe",
          |  "hobbies": [
          |    "skiing",
          |    "fishing","golf"
          |  ]
          |}
        """.stripMargin

      withClue("Same json no hal") {
        matchBodies(None, expected, expected)(IrNodeMatchingRules.empty, pactReaderInstance).isSuccess shouldEqual true
      }

      withClue("Same json + hal") {
        matchBodies(None, expected, expected)(IrNodeMatchingRules.empty, pactReaderInstance).isSuccess shouldEqual true
      }

      withClue("Expected compared to received") {
        matchBodies(None, expected, received)(IrNodeMatchingRules.empty, pactReaderInstance).isSuccess shouldEqual true
      }

    }

    it("should be able to match json bodies with rules") {

      val expected =
        """
          |{
          |  "name":"joe"
          |}
        """.stripMargin

      val received =
        """
          |{
          |  "name":"eirik"
          |}
        """.stripMargin

      val rules: Option[Map[String, MatchingRule]] = Option(
        Map(
          "$.body.name" -> MatchingRule(Option("regex"), Option("\\w+"), None)
        )
      )

      IrNodeMatchingRules.fromPactRules(rules) match {
        case Left(e) =>
          fail(e)

        case Right(r) =>
          withClue("Didn't match json body with rule") {
            matchBodies(None, expected, received)(r, pactReaderInstance).isSuccess shouldEqual true
          }
      }

    }

    it("should be able to match xml bodies") {

      val expected1 =
        <fish-supper>
          <fish>cod</fish>
          <chips>obviously</chips>
          <sauce>ketchup</sauce>
        </fish-supper>

      val received1 =
        <fish-supper>
          <fish>cod</fish>
          <chips>obviously</chips>
          <sauce>ketchup</sauce>
        </fish-supper>

      withClue("Same xml") {
        matchBodies(None, expected1.toString(), received1.toString())(
          IrNodeMatchingRules.empty,
          pactReaderInstance
        ).isSuccess shouldEqual true
      }

      val expected2 =
        <fish-supper>
          <fish>cod</fish>
          <chips>obviously</chips>
          <sauce>ketchup</sauce>
        </fish-supper>

      val received2 =
        <fish-supper>
          <fish>cod</fish>
          <chips>not too many...</chips>
          <sauce>ketchup</sauce>
        </fish-supper>

      withClue("Different xml") {
        matchBodies(None, expected2.toString(), received2.toString())(
          IrNodeMatchingRules.empty,
          pactReaderInstance
        ).isSuccess shouldEqual false
      }

      val expected3 =
        <fish-supper>
          <fish sustainable="true">cod</fish>
          <chips>obviously</chips>
          <sauce>ketchup</sauce>
        </fish-supper>

      val received3 =
        <fish-supper>
          <fish sustainable="true" oceanic="true">cod</fish>
          <chips>obviously</chips>
          <sauce>ketchup</sauce>
          <pickle>beetroot</pickle>
          <gravy/>
        </fish-supper>

      withClue("Received xml with additional fields and attributes") {
        matchBodies(None, expected3.toString(), received3.toString())(
          IrNodeMatchingRules.empty,
          pactReaderInstance
        ).isSuccess shouldEqual true
      }

      val expected4 =
        <fish-supper>
          <fish sustainable="true" oceanic="true">cod</fish>
          <chips>obviously</chips>
          <sauce>ketchup</sauce>
          <pickle>beetroot</pickle>
          <gravy/>
        </fish-supper>

      val received4 =
        <fish-supper>
          <fish sustainable="true">cod</fish>
          <chips>obviously</chips>
          <sauce>ketchup</sauce>
        </fish-supper>

      withClue("Received xml with missing fields and attributes") {
        matchBodies(None, expected4.toString(), received4.toString())(
          IrNodeMatchingRules.empty,
          pactReaderInstance
        ).isSuccess shouldEqual false
      }

    }

  }

  describe("Drift AKA Choosing the closest failed match") {

    it("should be able to pick the matching response") {

      val pactExpected: Pact =
        makePact(
          makeInteraction(
            "A",
            "200",
            """{"message": "Hello"}"""
          ),
          makeInteraction(
            "B",
            "404",
            """{"message": "Hello"}"""
          )
        )

      val pactActual: Pact =
        makePact(
          makeInteraction(
            "",
            "200",
            """{"message": "Hello"}"""
          )
        )

      val res: OutcomeAndInteraction = InteractionMatchers
        .matchOrFindClosestResponse(true, pactExpected.interactions, pactActual.interactions.head.response)
        .get

      res.closestMatchingInteraction.description shouldEqual "A"
      res.outcome shouldBe MatchOutcomeSuccess

    }

    it("should pick the last response were there is an equal amount of drift.") {

      val pactExpected: Pact =
        makePact(
          makeInteraction(
            "A",
            "500",
            """{"message": "Hello"}"""
          ),
          makeInteraction(
            "B",
            "404",
            """{"message": "Hello"}"""
          )
        )

      val pactActual: Pact =
        makePact(
          makeInteraction(
            "",
            "200",
            """{"message": "Hello"}"""
          )
        )

      val res: OutcomeAndInteraction = InteractionMatchers
        .matchOrFindClosestResponse(true, pactExpected.interactions, pactActual.interactions.head.response)
        .get

      res.closestMatchingInteraction.description shouldEqual "B"

      res.outcome match {
        case MatchOutcomeSuccess =>
          fail("Should not have matched")

        case f @ MatchOutcomeFailed(_, drift) =>
          f.errorCount shouldEqual 1
          drift shouldEqual 50
      }

    }

    it("should pick the closest match response were there is an unequal amount of drift. (case 1)") {

      val pactExpected: Pact =
        makePact(
          makeInteraction(
            "A",
            "500",
            """{"message": "Hello"}"""
          ),
          makeInteraction(
            "B",
            "404",
            """{"message": "Hello2"}"""
          )
        )

      val pactActual: Pact =
        makePact(
          makeInteraction(
            "",
            "200",
            """{"message": "Hello"}"""
          )
        )

      val res: OutcomeAndInteraction = InteractionMatchers
        .matchOrFindClosestResponse(true, pactExpected.interactions, pactActual.interactions.head.response)
        .get

      res.closestMatchingInteraction.description shouldEqual "A"

      res.outcome match {
        case MatchOutcomeSuccess =>
          fail("Should not have matched")

        case f @ MatchOutcomeFailed(_, drift) =>
          f.errorCount shouldEqual 1
          drift shouldEqual 50
      }

    }

    it("should pick the closest match response were there is an unequal amount of drift. (case 2)") {

      val pactExpected: Pact =
        makePact(
          makeInteraction(
            "A",
            "500",
            """{"message": "Hello"}"""
          ),
          makeInteraction(
            "B",
            "200",
            """{"message": "Hello2"}"""
          )
        )

      val pactActual: Pact =
        makePact(
          makeInteraction(
            "",
            "200",
            """{"message": "Hello"}"""
          )
        )

      val res: OutcomeAndInteraction = InteractionMatchers
        .matchOrFindClosestResponse(true, pactExpected.interactions, pactActual.interactions.head.response)
        .get

      res.closestMatchingInteraction.description shouldEqual "B"

      res.outcome match {
        case MatchOutcomeSuccess =>
          fail("Should not have matched")

        case f @ MatchOutcomeFailed(_, drift) =>
          f.errorCount shouldEqual 1
          drift shouldEqual 1
      }

    }

    def makeInteraction(description: String, status: String, body: String): String =
      s"""
          |    {
          |      "description" : "$description",
          |      "request" : {
          |        "method" : "GET",
          |        "path" : "/"
          |      },
          |      "response" : {
          |        "status" : $status,
          |        "body" : $body,
          |        "matchingRules": {}
          |      }
          |    }
        """.stripMargin

    def makePact(interactions: String*): Pact = {
      val json: String =
        s"""{
          |  "provider" : {
          |    "name" : "provider"
          |  },
          |  "consumer" : {
          |    "name" : "consumer"
          |  },
          |  "interactions" : [${interactions.toList.mkString(",")}]
          |}""".stripMargin

      pactReaderInstance.jsonStringToScalaPact(json) match {
        case Right(p) =>
          p

        case Left(s) =>
          throw new Exception(s)
      }
    }

  }

}
