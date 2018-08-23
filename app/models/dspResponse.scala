package models

case class dspResponse(request_id: String, url: String, price: Float)

object dspResponse{
  def apply(request_id: String, url: String, price: Float): dspResponse = {
    new dspResponse(request_id, url, price)
  }
}