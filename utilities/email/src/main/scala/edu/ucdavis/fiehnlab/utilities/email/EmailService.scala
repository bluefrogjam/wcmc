package edu.ucdavis.fiehnlab.utilities.email

import java.io.File

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.mail.javamail.{JavaMailSender, MimeMessageHelper}

@Configuration
class EmailServiceAutoConfiguration {

  @Bean
  def emailService: EmailService = new EmailService

}

/**
  * allows easy and convinient access to sending emails to remote users
  */
class EmailService {

  @Autowired
  private val sender: JavaMailSender = null

  def send(from: String, recipients: Seq[String], content: String, subject: String, attachment: Option[File]) = {

    recipients.foreach { x =>

      val message = sender.createMimeMessage()

      val helper = new MimeMessageHelper(message)
      helper.setFrom(from)
      helper.setText(content)
      helper.setSubject(subject)

      helper.setTo(x)

      if (attachment.isDefined) helper.addAttachment(attachment.get.getName, attachment.get)

      sender.send(message)
    }
  }
}
