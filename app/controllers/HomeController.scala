package controllers

import models.{WinNotice, dspResponse}
import javax.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import javax.inject.Inject
import scala.concurrent.{Await, Future}
import play.api.mvc._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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
          val responseList: List[Future[dspResponse]] = List(DspManager.requestToDsp(s, request1), DspManager.requestToDsp(s, request2))
          val futureOfList: Future[List[dspResponse]] = Future.sequence(responseList)

          val winReqestProcess: Future[String] = futureOfList.map{ x =>
            val winD: dspResponse = x.maxBy(_.price)
            //二番目に高い価格は最大価格のデータを取り除いた要素の最大価格
            val winPrice = x.filterNot(p => p.price == winD.price ).maxBy(_.price).price + 1
            val winUrl: String = winD.url
            val winNotice = WinNotice(winD.request_id, winPrice)
            DspManager.sendWinNotice(winNotice, ws.url(winD.url))
            winUrl
          }
          val winUrl: String = Await.result(winReqestProcess, Duration.Inf)
          //val sss: Option[Try[String]] = aa.value
          Ok(Json.toJson("url" -> winUrl))
        case None => BadRequest("argment error")
      }
    }
  }
}
