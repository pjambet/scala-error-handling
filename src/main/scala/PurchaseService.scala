import java.util.UUID

import WebServer.OrderRequest

import scala.concurrent.{ExecutionContext, Future}

object PurchaseService {

  final case class Order(orderId: String)

  def createPurchase(orderRequest: OrderRequest)(implicit ec: ExecutionContext): Future[Order] = {
    PaymentClient.chargeCard(orderRequest.cardNumber).map { _ =>
      Order(orderId = UUID.randomUUID().toString)
    }
  }

}
