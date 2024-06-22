package com.itv.scalapact.shared.json

import com.itv.scalapact.shared.Contract

trait IPactWriter {
  def pactToJsonString(pact: Contract, scalaPactVersion: String): String
}
