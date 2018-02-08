package com.bs.notifications

import scala.util.{Failure, Success}

trait EmailService extends Sys {

  private lazy val log = createLogger

  def sendEmails(emails: Seq[Email]) = {

    emails.foreach { email =>

      log.info(s"Attempting to send an email to ${email.emailAddress}.")

      import courier._
      import Defaults._

      val mailer = Mailer(smtpHost, smtpPort)
        .sslSocketFactory
        .auth(true)
        .as(userName, password)()

      mailer(Envelope.from(sourceEmail.addr)
        .to(email.emailAddress.addr)
        .subject(subject)
        .content(Text(email.content)))
        .onComplete {
          case Success(_) => log.info(s"Email has been sent.")
          case Failure(ex) => log.warning(s"Failed to send email: $ex")
        }
    }
  }


}
