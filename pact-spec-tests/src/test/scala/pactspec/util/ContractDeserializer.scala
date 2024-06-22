package pactspec.util

trait ContractDeserializer[P] {
  def read(jsonString: String): Either[String, P]
}

object ContractDeserializer {
  def apply[P](implicit ev: ContractDeserializer[P]): ContractDeserializer[P] = ev

  implicit def pactDeserializer(implicit reader: PactReader): ContractDeserializer[Pact] = (jsonString: String) =>
    reader.jsonStringToScalaPact(jsonString)
}
