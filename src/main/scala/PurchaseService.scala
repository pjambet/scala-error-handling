import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

object PurchaseService {

  final case class Order(orderId: String)
  final case class PurchaseError(reason: String)

  def createPurchase(cardNumber: String)(
      implicit ec: ExecutionContext): Future[Either[PurchaseError, Order]] = {
    PaymentClient.chargeCard(cardNumber).map {
      case Right(_)    => Right(Order(orderId = UUID.randomUUID().toString))
      case Left(error) => Left(PurchaseError(error.declineCode))
    }
  }
}
