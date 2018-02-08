package com.bs.notifications

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

trait EmailTemplateService {

  implicit val ordering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))

  def createTemplate(name: String, messages: Seq[SqsMsgBody]): String = {
    val rows = messages.map { t =>
      val date = LocalDateTime.parse(t.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      val parsedDate = DateTimeFormatter.ofPattern("EEEE, HH:mm:ss").format(date)
      (date, s"$parsedDate       ${t.message}")
    }.sortBy(_._1)

    s"Hi $name, your friends are active!\n\n${rows.map(_._2).mkString("\n")}"
  }

}
