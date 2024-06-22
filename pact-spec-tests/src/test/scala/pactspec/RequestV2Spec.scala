package pactspec

import pactspec.util.{PactSpecTester, StrictAndNonStrict, StrictOnly}

class RequestV2Spec extends PactSpecTester {

  val pactSpecVersion = "2"

  describe("Exercising request V" + pactSpecVersion + " Pact Specification match tests") {

    it("should check the request body specs") {
      testRequestSpecs(
        List(
          fetchRequestSpec("/request/body/array at top level xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array at top level.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array in different order xml.json")(StrictOnly),
          fetchRequestSpec("/request/body/array in different order.json")(StrictOnly),
          fetchRequestSpec("/request/body/array size less than required xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array size less than required.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array with at least one element matching by example xml.json")(
            StrictAndNonStrict
          ),
          fetchRequestSpec("/request/body/array with at least one element matching by example.json")(
            StrictAndNonStrict
          ),
          fetchRequestSpec("/request/body/array with at least one element not matching example type.json")(StrictOnly),
          fetchRequestSpec("/request/body/array with nested array that does not match.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array with nested array that matches.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array with regular expression in element xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array with regular expression in element.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/array with regular expression that does not match in element xml.json")(
            StrictOnly
          ),
          fetchRequestSpec("/request/body/array with regular expression that does not match in element.json")(
            StrictOnly
          ),
          fetchRequestSpec("/request/body/different value found at index xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/different value found at index.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/different value found at key xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/different value found at key.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/empty body no content type.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/empty body.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/empty found at key where not empty expected xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches with regex with bracket notation xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches with regex with bracket notation.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches with regex xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches with regex.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches with type.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches with floats.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/matches with integers.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/missing body found when empty expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/missing body no content type.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/missing body.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/missing index xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/missing index.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/missing key xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/missing key.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/no body no content type xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/no body no content type.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/no body xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/no body.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/non empty body found when empty expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/not empty found at key when empty expected xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/not empty found in array when empty expected xml.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/not null found at key when null expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/not null found in array when null expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/null body no content type.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/null body.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/null found at key where not null expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/null found in array when not null expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/number found at key when string expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/number found in array when string expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/plain text that does not match.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/plain text that matches.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/string found at key when number expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/string found in array when number expected.json")(StrictAndNonStrict),
          fetchRequestSpec("/request/body/unexpected index with missing value xml.json")(StrictOnly),
          fetchRequestSpec("/request/body/unexpected index with non-empty value xml.json")(StrictOnly),
          fetchRequestSpec("/request/body/unexpected index with not null value.json")(StrictOnly),
          fetchRequestSpec("/request/body/unexpected index with null value.json")(StrictOnly),
          fetchRequestSpec("/request/body/unexpected key with empty value xml.json")(StrictOnly),
          fetchRequestSpec("/request/body/unexpected key with non-empty value xml.json")(StrictOnly),
          fetchRequestSpec("/request/body/unexpected key with not null value.json")(StrictOnly),
          fetchRequestSpec("/request/body/unexpected key with null value.json")(StrictOnly),
          fetchRequestSpec("/request/body/value found in array when empty expected xml.json")(StrictAndNonStrict)
        )
      )
    }
  }

}
