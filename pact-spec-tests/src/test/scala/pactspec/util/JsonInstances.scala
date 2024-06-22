package pactspec.util

import com.itv.scalapact.shared.Pact
import com.itv.scalapact.shared.json.{ContractDeserializer, IPactReader}
import io.circe.parser.parse
import pactspec.util.PactImplicits
import pactspec.util.PactReader

trait JsonInstances {
  import PactImplicits._

  implicit val pactReaderInstance: IPactReader = new PactReader

  implicit val pactDeserializer: ContractDeserializer[Pact] = (jsonString: String) =>
    parse(jsonString).flatMap(_.as[Pact]) match {
      case Right(a) => Right(a)
      case Left(_)  => Left(s"Could not read scala-pact pact from json: $jsonString")
    }

}
