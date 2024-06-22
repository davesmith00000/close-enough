package com.itv.scalapact.shared.matchir

import com.itv.scalapact.shared.matchir.IrNodeEqualityResult.{IrNodesEqual, IrNodesNotEqual}

import scala.xml.Elem
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class XmlEqualitySpec extends AnyFunSpec with Matchers {

  implicit class ElemOps(val elem: Elem) {
    def toNode = MatchIr.fromXml(elem)
  }

  def check(res: IrNodeEqualityResult): Unit =
    res match {
      case _: IrNodesEqual.type =>
        ()

      case e: IrNodesNotEqual =>
        fail(e.renderDifferences)
    }

  describe("testing the equality of xml objects") {

    it("should find equality of a simple example") {

      val expected = <fish><type>cod</type><side/></fish>.toNode
      val received = <fish><type>cod</type><side/></fish>.toNode

      check(expected =~ received)

    }

    it("should not find equality of a simple unequal example") {

      val expected2 = <fish><type>cod</type><side>chips</side></fish>.toNode
      val received2 = <fish><type>cod</type><side/></fish>.toNode

      withClue("Unequal") {
        (expected2 =~ received2).isEqual shouldEqual false
      }

    }

    it("should find equality when the right contains the left example") {

      val expected = <ns:fish battered="true"><type sustainable="false">cod</type><side>chips</side></ns:fish>.toNode
      val received =
        <ns:fish battered="true"><type sustainable="false" oceananic="true">cod</type><side>chips</side><sauce>ketchup</sauce></ns:fish>.toNode

      check(expected =~ received)

    }

    it("should not find equality when namespaces do not match") {

      // Note, the <sid> tag *is* equal since the left has less information than the right.
      val expected = <ns:fish><type>haddock</type><side/></ns:fish>.toNode
      val received = <fish><type>haddock</type><side>chips</side></fish>.toNode

      (expected =~ received).isEqual shouldEqual false

    }

  }

}
