package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonNode}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
  * defines an ion
  */
case class Ion(mass: Double, intensity: Float) {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Ion]

  override def toString(): String = s"${mass}:${intensity}"

  override def equals(other: Any): Boolean = other match {
    case that: Ion =>
      (that canEqual this) &&
        mass == that.mass &&
        intensity == that.intensity
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(mass, intensity)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

/**
  * which ion mode the spectra is
  */
@JsonDeserialize(using = classOf[IonModeDeserializer])
class IonMode(val mode: String)

case class PositiveMode() extends IonMode("positive")

case class NegativeMode() extends IonMode("negative")

case class UnknownMode() extends IonMode("unknown")


class IonModeDeserializer extends JsonDeserializer[IonMode] {
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext) = {
    val oc = jsonParser.getCodec
    val node: JsonNode = oc.readTree(jsonParser)

    val value = node.get("mode").textValue().toLowerCase

    if (value.startsWith("p")) {
      PositiveMode()
    } else if (value.startsWith("+")) {
      PositiveMode()
    } else if (value.startsWith("n")) {
      NegativeMode()
    } else if (value.startsWith("-")) {
      NegativeMode()}
    else {
      UnknownMode()
    }
  }
}
