package com.itv.scalapact.shared.json

import com.itv.scalapact.shared.matchir.IrNode

// TODO: Rename, this is the string to irnode thing.
trait IPactReader {

  def fromJSON(jsonString: String): Option[IrNode]

}
