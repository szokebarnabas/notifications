package com.bs.notifications

import akka.stream.alpakka.sqs.SqsSourceSettings
import akka.stream.alpakka.sqs.scaladsl.SqsSource
import akka.stream.scaladsl.Sink
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, Message}
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class SqsToEmailStreamer extends EmailTemplateService with EmailService with Sys {

  import org.json4s.jackson.Serialization.read

  implicit val sqsClient: AmazonSQSAsync = AmazonSQSAsyncClientBuilder
    .standard()
    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecret)))
    .withEndpointConfiguration(new EndpointConfiguration(sqsUrl, region))
    .build()

  private val settings = SqsSourceSettings(waitTimeSeconds = 1, maxBufferSize = Int.MaxValue, maxBatchSize = 10)
  private lazy val log = createLogger

  def handleRequest(ctx: Context): String = {

    log.info("Lambda has been triggered")
    val sqsMessages = SqsSource(sqsUrl, settings)
      .take(getMsgNumber)
      .runWith(Sink.seq)

    val deleteResult = for {
      sqsMsg <- sqsMessages
      msgBodies <- Future(extractBody(sqsMsg))
      msgsOfUsr = msgBodies.groupBy(_.email)
      msg = toEmail(msgsOfUsr)
      _ = sendEmails(msg)
      deleteResult = deleteFromSqsQueue(sqsMsg)
    } yield deleteResult

    deleteResult onComplete {
      case Success(_) => log.info(s"Streaming completed")
      case Failure(ex) => log.info("Streaming failed", ex)
    }

    Await.result(deleteResult, 5 minutes)

    "done"
  }

  private def getMsgNumber = {
    val attributeName = "ApproximateNumberOfMessages"
    sqsClient.getQueueAttributes(sqsUrl, List(attributeName).asJava) match {
      case res => res.getAttributes.get(attributeName).toInt
    }
  }

  private def extractBody(msg: Seq[Message]): Seq[SqsMsgBody] = {
    msg
      .map(sqsMsg => read[SqsNotification](sqsMsg.getBody))
      .map(body => read[SqsMsgBody](body.Message))
  }

  private def toEmail(msgListOfUsr: Map[String, Seq[SqsMsgBody]]): Seq[Email] = {
    msgListOfUsr.map { msgsOfUsr =>
      val email = msgsOfUsr._1
      val messageList = msgsOfUsr._2
      val template = createTemplate(messageList.head.name, messageList)
      Email(email, template)
    }.toSeq
  }

  private def deleteFromSqsQueue(messages: Seq[Message]) = {
    messages.map { msg =>
      sqsClient.deleteMessage(new DeleteMessageRequest(sqsUrl, msg.getReceiptHandle))
    }
  }

}

case class SqsMsgBody(timestamp: String, message: String, name: String, email: String)
case class SqsNotification(Message: String)
case class Email(emailAddress: String, content: String)