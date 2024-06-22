package com.itv.scalapact.shared.json

import com.itv.scalapact.shared.Pact
import com.itv.scalapact.shared.matchir.IrNode

trait IPactReader {

  def fromJSON(jsonString: String): Option[IrNode]

  def jsonStringToScalaPact(json: String): Either[String, Pact]

}
