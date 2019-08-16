package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action.charting

case class Chart(target: String,
                 datasets: Seq[Dataset],
                 markers: Seq[Marker],
                 zones: Seq[Zone])

case class Dataset(`type`: String,
                   rts: Seq[Double],
                   ints: Seq[Double])

case class Marker(`type`: String,
                  rt: Double,
                  int: Double)

case class Zone(name: String,
                start: Double,
                end: Double)
