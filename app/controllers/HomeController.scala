package controllers

import Models.{WinNotice, dspRequest, dspResponse}
import javax.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import javax.inject.Inject
import play.api.libs.ws.JsonBodyWritables._

import scala.concurrent.{Await, Future}
import play.api.mvc._
import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.{FileOutputStream => FileStream, OutputStreamWriter => StreamWriter}

@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {
  val DSPurl: String = "http://localhost:8080/"
  val request1: WSRequest = ws.url(DSPurl)
  val DSPurl2: String = "http://localhost:8081"
  val request2: WSRequest = ws.url(DSPurl2)

  //curl http://localhost:9000/req -X POST -H "Content-Type: application/json" -d '{"app_id": 123}'

  //SDKからリクエストを受け取る
  def index: Action[JsValue] = Action(parse.json).async { request =>
    Future {
      (request.body \ "app_id").asOpt[Int] match {
        case Some(s) =>
          //DSPの台数が増えた場合にはここに追加する
          val responseList: List[Future[dspResponse]] = List(requestToDsp(s, request1), requestToDsp(s, request2))
          val futureOfList: Future[List[dspResponse]] = Future.sequence(responseList)

          val aa: Future[String] = futureOfList.map{ x =>
            val winD: dspResponse = x.maxBy(_.price)
            //二番目に高い価格は最大価格のデータを取り除いた要素の最大価格
            //DSPが1つの時の処理も書き加える必要がある
            val winPrice = x.filterNot(p => p.price == winD.price ).maxBy(_.price).price
            val winUrl: String = winD.url
            val winNotice = WinNotice(winD.request_id, winPrice)
            sendWinNotice(winNotice, ws.url(winD.url))
            winUrl
          }
          val aaa = Await.result(aa, Duration.Inf)
          //val sss: Option[Try[String]] = aa.value
          Ok(Json.toJson("url" -> aaa))
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

  //勝ったDSPにDataを送る
  //この中でlog生成
  def sendWinNotice(postValue: WinNotice, request: WSRequest): Unit ={
    val complexRequest =
      request.addHttpHeaders ("Accept" -> "application/json")
        .withRequestTimeout(100.millis)
        .post (Json.toJson(postValue))
    writeLog(postValue)
  }

  //ログファイルに書き込み
  //WinNoticeのmodelを受け取ってパースすればええやろ
  def writeLog(value: WinNotice): Unit ={
    val fileName = "log1.txt"
    val encode = "UTF-8"
    val append = true
    // 書き込み処理
    val fileOutPutStream = new FileStream(fileName, append)
    val writer = new StreamWriter( fileOutPutStream, encode )
    writer.write("reqest_id : "+value.request_id+"-----price : "+value.price+"\n")
    writer.close
  }
}
