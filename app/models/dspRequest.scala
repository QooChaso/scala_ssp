package models

import java.time.LocalDateTime
import play.api.libs.json.{Json, Writes}

case class dspRequest(ssp_name: String, request_time: String, request_id: String, app_id: Int)

object dspRequest{

  def apply(appId: Int): dspRequest = {
    val sspName: String = "f_ssp"
    val requestTimeToDsp: String = LocalDateTime.now.toString
    val requestId: String = java.util.UUID.randomUUID().toString + requestTimeToDsp
    new dspRequest(sspName, requestTimeToDsp, requestId, appId)
  }

  implicit lazy val jsonWrites = new Writes[dspRequest] {
    def writes(d: dspRequest) =
      Json.obj(
        "ssp_name" -> d.ssp_name,
        "request_time" -> d.request_time,
        "request_id" -> d.request_id,
        "app_id" -> d.app_id
      )
  }
}
