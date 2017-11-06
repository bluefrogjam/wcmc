package edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample

/**
  * defines an ion
  */
case class Ion(mass: Double, intensity: Double) {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Ion]

  override def toString():String = s"${mass}:${intensity}"

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
class IonMode(val mode: String)

case class PositiveMode() extends IonMode("positive")

case class NegativeMode() extends IonMode("negative")

case class Unknown() extends IonMode("unknown")
