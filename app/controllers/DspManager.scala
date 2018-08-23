package controllers

import models.{WinNotice, dspRequest, dspResponse}
import akka.actor.ActorSystem
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest


import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object DspManager {

  //DSPにRequestを送りResponseが返る
  def requestToDsp(appId: Int, request: WSRequest): Future[dspResponse] ={
    implicit val system = ActorSystem ("HomeController")

    val postValue = Json.toJson(dspRequest.apply(appId))

    val complexRequest =
      request.addHttpHeaders ("Accept" -> "application/json")
        .withRequestTimeout(100.millis)
        .post (postValue)
        .map { response => dspResponse.apply (//Right(dspResponse.apply (
          (response.json \ "request_id").get.as[String],
          (response.json \ "url").get.as[String],
          (response.json \ "price").get.as[Float])}
    //.recover { case e => Left(e) }

    complexRequest
  }

  //勝ったDSPにDataを送る
  def sendWinNotice(postValue: WinNotice, request: WSRequest): Unit ={
    val complexRequest =
      request.addHttpHeaders ("Accept" -> "application/json")
        .withRequestTimeout(100.millis)
        .post (Json.toJson(postValue))
    //ログ生成
    LogManager.writeLog(postValue)
  }
}
