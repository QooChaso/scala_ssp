package controllers

import javax.inject._
import play.api.libs.json.JsValue
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  //curl http://localhost:9000/ -X POST -H "Content-Type: application/json" -d '{"app_id": 123}'
  //SDKからリクエストを受け取る
  def index: Action[JsValue] = Action(parse.json) { request =>
    (request.body \ "app_id").asOpt[Int].map { appId =>
      //ここでDSPManagerに値を渡す

      //最後にここでurlを返す
      Ok("Hello " + appId)
    }.getOrElse {
      BadRequest("Missing parameter [app_id]")
    }
  }

}
