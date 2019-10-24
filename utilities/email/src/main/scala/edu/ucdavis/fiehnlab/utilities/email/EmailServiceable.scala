package edu.ucdavis.fiehnlab.utilities.email

import java.io.File

trait EmailServiceable {

  def send(from: String, recipients: Seq[String], content: String, subject: String, attachment: Option[File])
}
