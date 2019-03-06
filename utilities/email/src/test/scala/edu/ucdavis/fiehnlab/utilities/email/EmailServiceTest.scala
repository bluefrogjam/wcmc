package edu.ucdavis.fiehnlab.utilities.email

import java.io.{File, PrintWriter}
import java.util.Properties

import ch.qos.logback.core.util.FileUtil
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.javamail.{JavaMailSender, JavaMailSenderImpl}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest

@RunWith(classOf[SpringRunner])
@SpringBootTest
class EmailServiceTest extends WordSpec with BeforeAndAfter with Matchers {

  @Autowired
  val emailService: EmailService = null

  @Autowired
  val emailSender: JavaMailSenderImpl = null

  val testSmtp = new GreenMail(ServerSetupTest.SMTP)

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "EmailServiceTest" should {

    "prepare" must {


      testSmtp.start

      //don't forget to set the test port!
      emailSender.setPort(3025)
      emailSender.setHost("localhost")


      "send without a attachment" in {
        emailService.send("test@test.de", "test@test.com" :: List(), "none", "no subject", None)

        val messages = testSmtp.getReceivedMessages

        messages.length shouldBe( 1)
        messages(0).getSubject.toString shouldBe("no subject")
      }

      "send with an attachment" in {
        val file = File.createTempFile("test","test")

        new PrintWriter(file) {
          write("tada")
          close()
        }

        emailService.send("test@test.de", "test@test.com" :: List(), "none", "no subject", Option(file))

        val messages = testSmtp.getReceivedMessages

        messages.length shouldBe( 1 + 1)
        messages(1).getSubject.toString shouldBe("no subject")
        messages(0).getSize should be < messages(1).getSize
      }


      "send several messages" in {
        emailService.send("test@test.de", "test@test.com" :: "test@test.de" :: List(), "none", "no subject", None)

        val messages = testSmtp.getReceivedMessages

        messages.length shouldBe( 2 + 1 + 1)
      }
    }

  }

  protected def before(fun: => Any): Unit = {
    testSmtp.start()
  }

  protected def after(fun: => Any): Unit = {
    testSmtp.stop()
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class EmailServiceTestConfig {

}
