package controllers

import models.{WinNotice, dspResponse}
import javax.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import javax.inject.Inject

import scala.concurrent.{Await, Future, TimeoutException}
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {
  val DSPurl: String = "http://localhost:8080"
  val request1: WSRequest = ws.url(DSPurl+"/req")
  val DSPurl2: String = "http://localhost:8081"
  val request2: WSRequest = ws.url(DSPurl2+"/req")

  //curl http://localhost:9000/req -X POST -H "Content-Type: application/json" -d '{"app_id": 123}'

  //SDKからリクエストを受け取る
  def index: Action[JsValue] = Action(parse.json).async { request =>
    Future {
      (request.body \ "app_id").asOpt[Int] match {
        case Some(s) =>
          //DSPの台数が増えた場合にはここに追加する
          val responseList: List[Future[Option[dspResponse]]] =
            List(DspManager.requestToDsp(s, request1, DSPurl), DspManager.requestToDsp(s, request2, DSPurl2))
          val futureOfList: Future[List[Option[dspResponse]]] = Future.sequence(responseList)
          val hoge: Future[List[dspResponse]] = futureOfList.map{ x => x.flatten }
          val winReqestProcess: Future[String] = hoge.map{ x =>
            var winPrice: Float = 0
            val winD: dspResponse = x.maxBy(_.price)
            //セカンドプライス = 最大価格のデータを取り除いた要素の最大価格 + 1
            if(x.length >= 2) winPrice = x.filterNot(p => p.price == winD.price ).maxBy(_.price).price + 1
            else winPrice += 1
            val winUrl: String = winD.url
            val winNotice = WinNotice(winD.request_id, winPrice)
            DspManager.sendWinNotice(winNotice, ws.url(winD.reqUrl))
            winUrl
          }
          val winUrl: String = Await.result(winReqestProcess, Duration.Inf)
          Ok(Json.toJson("url" -> winUrl))
        case None => BadRequest("argment error")
      }
    }
  }
}
