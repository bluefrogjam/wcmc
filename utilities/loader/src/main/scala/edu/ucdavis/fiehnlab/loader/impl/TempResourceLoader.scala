package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}

import edu.ucdavis.fiehnlab.loader.LocalLoader
import org.springframework.stereotype.Component

/**
	* Created by diego on 7/13/2017.
	*/
@Component
class TempResourceLoader extends LocalLoader {
	/**
		* returns the related resource or none
		*
		* @param name
		* @return
		*/
	override def load(name: String): Option[InputStream] = {
		val dir = new File(System.getProperty("java.io.tmpdir"))
		val file = new File(dir, name)

		if(exists(file.getAbsolutePath)){
			logger.debug("\tResource found in temp")
			Option(new FileInputStream(file))
		} else {
			logger.debug(s"\tResource not found in temp: ${file.getAbsolutePath}")
			None
		}
	}

	/**
		* does the given resource exists
		*
		* @param name
		* @return
		*/
	override def exists(name: String): Boolean = {
//		val fileString = System.getProperty("java.io.tmpdir") + name

		return new File(name).exists()
	}

	override def priority: Int = super.priority - 100
}
