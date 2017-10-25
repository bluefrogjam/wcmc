package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}

import edu.ucdavis.fiehnlab.loader.LocalLoader
import org.springframework.beans.factory.annotation.Autowired

/**
	* searches directories recursivly to find resources and returns the input stream to them, if found
	*/
class RecursiveDirectoryResourceLoader @Autowired()(directory: File, override val priority: Int = 0) extends LocalLoader {

	if (!directory.exists()) {
		logger.info(s"making directory: ${directory.getAbsolutePath}")
		directory.mkdirs()
	}
	else {
		logger.debug(s"lookup folder is: ${directory.getAbsolutePath}")
	}

	/**
		* returns the related resource or none
		*
		* @param name
		* @return
		*/
	override def load(name: String): Option[InputStream] = {
		val result = loadAsFile(name)

		if (result.isDefined) {
			Option(new FileInputStream(result.get))
		}
		else {
			None
		}
	}

	/**
		* @param name
		* @return
		*/
	override def loadAsFile(name: String): Option[File] = {
		logger.debug(s"recursively load ${name} from ${directory}")

		val files = walkTree(directory).filter(p => p.getAbsolutePath.endsWith(name) && p.isFile)
		files.headOption
	}

	override def toString = s"RecursiveDirectoryResourceLoader(directory: ${directory.getAbsolutePath})"

	override def exists(name: String): Boolean = walkTree(directory).exists(p => p.getAbsolutePath.endsWith(name))

	private final def walkTree(file: File): Iterable[File] = {
		val children = new Iterable[File] {
			def iterator: Iterator[File] = if (file.isDirectory && file.listFiles().length > 0) {
				file.listFiles.iterator
			} else Iterator.empty
		}

		Seq(file) ++: children.flatMap(walkTree)
	}
}
