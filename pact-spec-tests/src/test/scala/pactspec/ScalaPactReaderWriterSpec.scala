package pactspec

import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import pactspec.util.PactReader

class ScalaPactReaderWriterSpec extends AnyFunSpec with Matchers with OptionValues {

  val pactReader = new PactReader

  val scalaPactVersion: String = "1.0.0"

  describe("Reading and writing a homogeneous Pact files") {

    it("should be able to read Pact files") {
      val pactEither = pactReader.jsonStringToScalaPact(PactFileExamples.simpleAsString)

      pactEither.toOption.value shouldEqual PactFileExamples.simple
    }

    it("should be able to read Pact files using the old provider state key") {
      val pactEither = pactReader.jsonStringToScalaPact(PactFileExamples.simpleOldProviderStateAsString)

      pactEither.toOption.value shouldEqual PactFileExamples.simple
    }

    it("should be able to read ruby format json") {
      val pactEither = pactReader.jsonStringToScalaPact(PactFileExamples.simpleAsString)

      pactEither.toOption.value shouldEqual PactFileExamples.simple
    }

    it("should be able to read ruby format json with no body") {
      val pactEither = pactReader.jsonStringToScalaPact(PactFileExamples.verySimpleAsString)

      pactEither.toOption.value shouldEqual PactFileExamples.verySimple
    }

    it("should be able to parse another example") {

      pactReader.jsonStringToScalaPact(PactFileExamples.anotherExample) match {
        case Left(e) =>
          fail(e)

        case Right(pact) =>
          pact.consumer.name shouldEqual "My Consumer"
          pact.provider.name shouldEqual "Their Provider Service"
          pact.interactions.head.response.body.get shouldEqual "Hello there!"
      }

    }

    it("should be able to parse _links and metadata") {
      val pactEither = pactReader.jsonStringToScalaPact(PactFileExamples.simpleWithLinksAndMetaDataAsString)

      pactEither.toOption.value shouldEqual PactFileExamples.simpleWithLinksAndMetaData
    }
  }
}
