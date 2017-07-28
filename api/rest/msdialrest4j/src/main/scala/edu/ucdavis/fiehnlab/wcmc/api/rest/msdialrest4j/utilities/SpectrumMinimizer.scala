package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.utilities

import java.io._

import com.typesafe.scalalogging.LazyLogging
import org.springframework.stereotype.Component

import scala.io.Source

/**
	* Created by diego on 7/28/2017.
	*/
@Component
class SpectrumMinimizer extends LazyLogging {
	def minimize(file: File): File = {
		val outFile = new File(s"${file.getAbsolutePath}.fixed")
		val outStream: FileOutputStream = new FileOutputStream(outFile)

		var linemax: Int = 0
		var maxSpec: Int = 0

		var headers: Array[String] = null
		Source.fromFile(file).getLines().foreach { line =>
			if (line.startsWith("PeakID")) {
				headers = line.toLowerCase().split("\t")
				outStream.write(line.getBytes())
				outStream.write('\n')
				outStream.flush()
			} else {
				val dataMap = (headers zip line.split("\t")).toMap
				linemax = Math.max(linemax, dataMap("ms1 spectrum").length)

				var basePeakInt = 0D
				val ions: Seq[(Double, Double)] = dataMap("ms1 spectrum").split(" ").filter(_.nonEmpty)
					.collect {
						case x: String if x.nonEmpty =>
							val pair = x.split(":")
							basePeakInt = Math.max(basePeakInt, pair(1).toDouble)
							(pair(0).toDouble, pair(1).toDouble)
					}

				val threshold = basePeakInt * 0.001

				outStream.write(dataMap("peakid").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("title").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("scans").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("rt(min)").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("precursor m/z").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("height").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("area").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("metabolitename").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("model masses").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("adduction").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("isotope").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("smiles").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("inchikey").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("dot product").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("reverse dot product").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("fragment presence %").getBytes())
				outStream.write('\t')
				outStream.flush()
				outStream.write(dataMap("total score").getBytes())
				outStream.write('\t')
				outStream.flush()

				outStream.write(ions.filter(_._2 > threshold).map(i => s"${i._1}:${i._2}").mkString(" ").getBytes())
				outStream.write('\t')
				outStream.flush()

				if (dataMap.keySet.contains("msms spectrum"))
					outStream.write(dataMap("msms spectrum").getBytes())
				outStream.write('\n')
				outStream.flush()

			}
		}

		outStream.flush()
		outStream.close()

		outFile
	}
}
