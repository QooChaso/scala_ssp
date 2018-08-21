package controllers

import Models.{WinNotice, dspRequest, dspResponse}
import javax.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import javax.inject.Inject

import scala.concurrent.Future
import play.api.mvc._
import akka.actor.ActorSystem

import scala.concurrent.duration._

@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {
  val DSPurl: String = "http://dsp1.example.jp/req"
  val DSPurl2: String = "http://dsp1.example.jp/req"
  val request1: WSRequest = ws.url(DSPurl)
  val request2: WSRequest = ws.url(DSPurl2)

  //curl http://localhost:9000/req -X POST -H "Content-Type: application/json" -d '{"app_id": 123}'
  //SDKからリクエストを受け取る
  def index: Action[JsValue] = Action(parse.json).async { request =>
    Future {
      (request.body \ "app_id").asOpt[Int] match {
        case Some(s) =>
          val hoge = List(requestToDsp(s, request1), requestToDsp(s, request1))
          val futureOfList = Future.sequence(hoge).map{ ddd =>
            val winD = ddd.maxBy(_.price)
            //二番目に高い価格は最大価格のデータを取り除いた要素の最大価格
            val winPrice = ddd.filterNot(p => p.price == winD.price ).maxBy(_.price).price
            val winNotice = makeWinNotice(winD, winPrice)
          }

          //ここでWinNoticeをPOSTで送る


          //ここでSDKにurlのみを返す
          Ok("")
        case None => BadRequest("argment error")
      }
    }
  }

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

  def makeWinNotice(win: dspResponse, secondPrice: Float): WinNotice ={
    new

  }

  //勝ったDSPにDataを送る
  //この中でlog生成
  def sendWinNotice(postValue: WinNotice, request: WSRequest): Unit ={
    val complexRequest =
      request.addHttpHeaders ("Accept" -> "application/json")
        .withRequestTimeout(100.millis)
        .post (postValue)
  }

  //ログファイルに書き込み
  //WinNoticeのmodelを受け取ってパースすればええやろ
  def writeLog(): Unit ={}

}
