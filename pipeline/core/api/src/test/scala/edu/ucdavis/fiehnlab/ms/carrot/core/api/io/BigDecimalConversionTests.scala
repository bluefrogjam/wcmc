package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.ApiConfig
import org.scalatest.{Matchers, WordSpec}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration], classOf[ApiConfig]))
@ActiveProfiles(Array("test"))
class BigDecimalConversionTests extends WordSpec with Matchers {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BigDecimal" should {

    "convert a floting point twisted number into a double" in {
      val number: Object = new java.lang.Double(376.39599999999996)
      number shouldBe a[Object]
      number shouldBe a[java.lang.Double]

      val strCeil = BigDecimal(number.toString).setScale(4, BigDecimal.RoundingMode.CEILING).toDouble
      val dblCeil = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.CEILING).toDouble
      val strUp = BigDecimal(number.toString).setScale(4, BigDecimal.RoundingMode.UP).toDouble
      val dblUp = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.UP).toDouble
      val strHalfUp = BigDecimal(number.toString).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
      val dblHalfUp = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
      val strHalfEven = BigDecimal(number.toString).setScale(4, BigDecimal.RoundingMode.HALF_EVEN).toDouble
      val dblHalfEven = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.HALF_EVEN).toDouble
      val strHalfDown = BigDecimal(number.toString).setScale(4, BigDecimal.RoundingMode.HALF_DOWN).toDouble
      val dblHalfDown = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.HALF_DOWN).toDouble
      val strDown = BigDecimal(number.toString).setScale(4, BigDecimal.RoundingMode.DOWN).toDouble
      val dblDown = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.DOWN).toDouble
      val strFloor = BigDecimal(number.toString).setScale(4, BigDecimal.RoundingMode.FLOOR).toDouble
      val dblFloor = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.FLOOR).toDouble

      println("\n\n")
      println(s"CEILING (string): $strCeil")
      println(s"CEILING (scala): $dblCeil")
      println(s"UP (string): $strUp")
      println(s"UP (scala): $dblUp")
      println(s"HALF_UP (string): $strHalfUp")
      println(s"HALF_UP (scala): $dblHalfUp")
      println(s"HALF_EVEN (string): $strHalfEven")
      println(s"HALF_EVEN (scala): $dblHalfEven")
      println(s"HALF_DOWN (string): $strHalfDown")
      println(s"HALF_DOWN (scala): $dblHalfDown")
      println(s"DOWN (string): $strDown")
      println(s"DOWN (scala): $dblDown")
      println(s"FLOOR (string): $strFloor")
      println(s"FLOOR (scala): $dblFloor")

      strCeil should equal(dblCeil)
      strUp should equal(dblUp)
      strHalfUp should equal(dblHalfUp)
      strHalfEven should equal(dblHalfEven)
      strHalfDown should equal(dblHalfDown)
      strDown should equal(dblDown)
      strFloor should equal(dblFloor)

      val dbl: Double = BigDecimal(number.asInstanceOf[Double]).setScale(4, BigDecimal.RoundingMode.CEILING).toDouble
      println(s"Double representation of number after BigDecimal: $dbl")

    }
  }
}
