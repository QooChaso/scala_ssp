package models

import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.JsonBodyWritables._


case class WinNotice(request_id: String, price: Float)

object WinNotice{
  def apply(request_id: String, price: Float): WinNotice ={
    new WinNotice(request_id, price)
  }

  implicit lazy val jsonWrites = new Writes[WinNotice] {
    def writes(d: WinNotice) =
      Json.obj(
        "request_id" -> d.request_id,
        "price" -> d.price,
      )
  }
}
