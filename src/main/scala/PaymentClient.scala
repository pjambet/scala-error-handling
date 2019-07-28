import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

object PaymentClient {

  final case class Charge(chargeId: UUID)
  final case class ChargeFailure(declineCode: String)

  final class PaymentException(message: String) extends Exception(message)

  def chargeCard(cardNumber: String)(
      implicit ec: ExecutionContext): Future[Either[ChargeFailure, Charge]] = {
    // Fake an API call
    Future {
      if (cardNumber == "valid") {
        Right(Charge(UUID.randomUUID()))
      } else if (cardNumber.startsWith("card_error")) {
        val declineCode = cardNumber.split("_").toList.drop(2).mkString("_")
        Left(ChargeFailure(declineCode))
      } else if (cardNumber == "rate_limit_error") {
        throw new PaymentException("rate_limit_error")
      } else if (cardNumber == "invalid_request_error") {
        throw new PaymentException("invalid_request_error")
      } else if (cardNumber == "authentication_error") {
        throw new PaymentException("authentication_error")
      } else if (cardNumber == "api_connection_error") {
        throw new PaymentException("api_connection_error")
      } else {
        throw new PaymentException("unknown_error")
      }
    }
  }
}
