import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

object PaymentClient {

  final case class Charge(chargeId: UUID)

  sealed trait PaymentException extends Throwable
  final class CardException(val declineCode: String) extends PaymentException
  final class CardExceptionWithExtraParam(val declineCode: String,
                                          val param: String)
      extends PaymentException
  final class NotEnoughFundException extends PaymentException
  final class RateLimitException extends PaymentException
  final class InvalidRequestException extends PaymentException
  final class AuthenticationException extends PaymentException
  final class APIConnectionException extends PaymentException
  final class UnknownPaymentException extends PaymentException

  def chargeCard(cardNumber: String)(
      implicit ec: ExecutionContext): Future[Charge] = {
    // Fake an API call
    Future {
      if (cardNumber == "valid") {
        Charge(UUID.randomUUID())
      } else if (cardNumber.startsWith("card_error")) {
        val declineCode = cardNumber.split("_").toList.drop(2).mkString("_")
        throw new CardException(declineCode)
      } else if (cardNumber.startsWith("card_error_for_zipcode")) {
        val declineCode = cardNumber.split("_").toList.drop(2).mkString("_")
        throw new CardExceptionWithExtraParam(declineCode, "zipcode")
      } else if (cardNumber == "rate_limit_error") {
        throw new RateLimitException()
      } else if (cardNumber == "invalid_request_error") {
        throw new InvalidRequestException()
      } else if (cardNumber == "authentication_error") {
        throw new AuthenticationException()
      } else if (cardNumber == "api_connection_error") {
        throw new APIConnectionException()
      } else {
        throw new UnknownPaymentException()
      }
    }
  }
}
