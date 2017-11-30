package edu.ucdavis.fiehnlab.utilities.email

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSenderImpl

import java.io.File
import java.util.Properties

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.mail.javamail.{JavaMailSender, MimeMessageHelper}

@Configuration
class EmailServiceAutoConfiguration {

  @Bean
  def emailService: EmailService = new EmailService

  @Bean
  def emailSender(@Value("${email.host}") emailHost: String, @Value("${email.port}") emailPort: Integer, @Value("${email.username}") username: String, @Value("${email.pass}") password: String): JavaMailSenderImpl = {
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

/**
  * allows easy and convinient access to sending emails to remote users
  */
class EmailService {

  @Autowired
  private val sender: JavaMailSender = null

  def send(from: String, recipients: Seq[String], content: String, subject: String, attachment: Option[File]) = {

    recipients.foreach { x =>

      val message = sender.createMimeMessage()

      val helper = new MimeMessageHelper(message,true)
      helper.setFrom(from)
      helper.setText(content)
      helper.setSubject(subject)

      helper.setTo(x)

      if (attachment.isDefined) helper.addAttachment(attachment.get.getName, attachment.get)

      sender.send(message)
    }
  }
}
