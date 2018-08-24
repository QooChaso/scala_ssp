package controllers

import models.{WinNotice, dspRequest, dspResponse}
import akka.actor.ActorSystem
import akka.dispatch.forkjoin.ForkJoinPool
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import play.shaded.ahc.io.netty.util.concurrent.DefaultThreadFactory

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Actor, ActorSystem, Props}
import scala.concurrent.{blocking, Future}

object DspManager {

  //DSPにRequestを送りResponseが返る
  //def requestToDsp(appId: Int, request: WSRequest, url: String):Future[Option[String]] ={
  def requestToDsp(appId: Int, request: WSRequest, url: String): Future[Option[dspResponse]] = {

  //val defaultEc: ExecutionContext = scala.concurrent.ExecutionContext.global
    //val forkJoinEc: ExecutionContext = ExecutionContext.fromExecutorService(new ForkJoinPool(50))
    //val forkJoinWithFactory: ExecutionContext = ExecutionContext.fromExecutorService(new ForkJoinPool(1000, new DefaultThreadFactory, uncaughtExceptionHandler, false))
   // implicit val system = ActorSystem("HomeController")
    //implicit val execContext : ExecutionContext =

    val postValue = Json.toJson(dspRequest.apply(appId))

    val sss: Future[dspResponse] =
      request.addHttpHeaders("Accept" -> "application/json")
        .withRequestTimeout(1.millis)
        .post(postValue)
        .map { response =>
          dspResponse.apply(
            (response.json \ "request_id").get.as[String],
            (response.json \ "url").get.as[String],
            (response.json \ "price").get.as[Float],
            url + "/win")
        }


    val complexRequest: Future[Option[dspResponse]] = sss.map(Some(_)).recover{case _ => None}
    complexRequest

    //Future(Option("aa"))
  }

  //勝ったDSPにDataを送る
  def sendWinNotice(postValue: WinNotice, request: WSRequest): Unit ={
    request.addHttpHeaders ("Accept" -> "application/json")
      .withRequestTimeout(100.millis)
      .post (Json.toJson(postValue))
    //ログ生成
    LogManager.writeLog(postValue)
  }
}
