package com.itv.scalapact.shared.json

import com.itv.scalapact.shared.Pact
import com.itv.scalapact.shared.matchir.IrNode

// import com.itv.scalapact.shared.{HALIndex, JvmPact, Pact, PactsForVerificationResponse}

trait IPactReader {

  def fromJSON(jsonString: String): Option[IrNode]

  def jsonStringToScalaPact(json: String): Either[String, Pact]

  // def jsonStringToJvmPact(json: String): Either[String, JvmPact]

  // def jsonStringToPactsForVerification(json: String): Either[String, PactsForVerificationResponse]

  // def jsonStringToHALIndex(json: String): Either[String, HALIndex]
}
