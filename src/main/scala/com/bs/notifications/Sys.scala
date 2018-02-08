package com.bs.notifications

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import org.json4s.{DefaultFormats, jackson}

trait Sys {
  import com.typesafe.config.ConfigFactory

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  private val conf = ConfigFactory.load()
  val awsConfig = conf.getConfig("aws")
  val awsAccessKey = awsConfig.getString("accessKey")
  val awsSecret = awsConfig.getString("secretKey")
  val region = awsConfig.getString("region")
  val sqsUrl = awsConfig.getString("sqsUrl")

  val emailConfig = conf.getConfig("email")
  val subject = emailConfig.getString("subject")
  val sourceEmail = emailConfig.getString("sourceEmail")
  val smtpHost = emailConfig.getString("host")
  val smtpPort = emailConfig.getInt("port")
  val userName = emailConfig.getString("userName")
  val password = emailConfig.getString("password")

  def createLogger: LoggingAdapter = Logging.getLogger(system, this)
}
