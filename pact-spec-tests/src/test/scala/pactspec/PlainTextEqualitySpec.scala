package pactspec

import pactspec.util.PlainTextEquality
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class PlainTextEqualitySpec extends AnyFunSpec with Matchers {

  describe("checking for plain text equality") {

    it("Should match equal strings") {

      PlainTextEquality.check("fish", "fish")
      PlainTextEquality.check("fish", " fish")
      PlainTextEquality.check("fish", "fish    ")
      PlainTextEquality.check("fish", "  fish    ")
      PlainTextEquality.check(" fish", "fish")

    }

  }

}
