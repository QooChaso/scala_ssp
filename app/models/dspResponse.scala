package models

case class dspResponse(request_id: String, url: String, price: Float, reqUrl: String)

object dspResponse{
  def apply(request_id: String, url: String, price: Float, url2: String): dspResponse = {
    new dspResponse(request_id, url, price, url2)
  }
}