package edu.ucdavis.fiehnlab.utilities.email

import java.io.File
import java.util.Properties

import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}
import org.springframework.mail.javamail.{JavaMailSender, JavaMailSenderImpl, MimeMessageHelper}
import org.springframework.stereotype.Component

@Configuration
@ComponentScan
class EmailServiceAutoConfiguration {

}

@Profile(Array("carrot.email.enable"))
@Configuration
class EmailEnabledConfiguration {

  @Bean
  def emailSender(@Value("${wcmc.email.host}") emailHost: String, @Value("${wcmc.email.port}") emailPort: Integer, @Value("${wcmc.email.username}") username: String, @Value("${wcmc.email.pass}") password: String): JavaMailSenderImpl = {
    val emailSender = new JavaMailSenderImpl
    emailSender.setHost(emailHost)
    emailSender.setPort(emailPort)
    emailSender.setUsername(username)
    emailSender.setPassword(password)
    //emailSender.setDefaultEncoding("UTF_8");
    val mailProps = new Properties()
    mailProps.setProperty("mail.transport.protocol", "smtp")
    mailProps.setProperty("mail.smtp.auth", "true")
    mailProps.setProperty("mail.smtp.starttls.enable", "true")
    mailProps.setProperty("mail.debug", "false")
    emailSender.setJavaMailProperties(mailProps)
    emailSender
  }
}

@Profile(Array("!carrot.email.enable"))
@Configuration
@Component
class NoEmailService extends EmailServiceable {
  override def send(from: String, recipients: Seq[String], content: String, subject: String, attachment: Option[File]): Unit = {}
}

/**
  * allows easy and convinient access to sending emails to remote users
  */
@Profile(Array("carrot.email.enable"))
@Configuration
@Component
class EmailService extends EmailServiceable {

  @Autowired
  private val sender: JavaMailSender = null

  override def send(from: String, recipients: Seq[String], content: String, subject: String, attachment: Option[File]) = {

    recipients.foreach { x =>

      val message = sender.createMimeMessage()

      val helper = new MimeMessageHelper(message, true)
      helper.setFrom(from)
      helper.setText(content)
      helper.setSubject(subject)

      helper.setTo(x)

      if (attachment.isDefined) helper.addAttachment(attachment.get.getName, attachment.get)

      sender.send(message)
    }
  }
}
