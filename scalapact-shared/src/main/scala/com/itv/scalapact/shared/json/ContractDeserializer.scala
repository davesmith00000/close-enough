package com.itv.scalapact.shared.json

import com.itv.scalapact.shared.Pact

trait ContractDeserializer[P] {
  def read(jsonString: String): Either[String, P]
}

object ContractDeserializer {
  def apply[P](implicit ev: ContractDeserializer[P]): ContractDeserializer[P] = ev

  implicit def pactDeserializer(implicit reader: IPactReader): ContractDeserializer[Pact] = (jsonString: String) =>
    reader.jsonStringToScalaPact(jsonString)
}
