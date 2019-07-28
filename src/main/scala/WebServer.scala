import PurchaseService.Order
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.util.{Failure, Success}

object WebServer {

  final case class OrderRequest(cardNumber: String)

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    implicit val orderFormat = jsonFormat1(Order)
    implicit val orderRequestFormat = jsonFormat1(OrderRequest)

    val route =
      path("order") {
        post {
          entity(as[OrderRequest]) { orderRequest =>
            onComplete(PurchaseService.createPurchase(orderRequest.cardNumber)) {
              case Success(Right(result)) =>
                complete(StatusCodes.Created, result)
              case Success(Left(error)) =>
                val jsonError = "{\"error\": \"" + error.reason + "\"}"
                complete(StatusCodes.PaymentRequired,
                         HttpEntity(ContentTypes.`application/json`, jsonError))
              case Failure(exception) =>
                system.log.error(exception, "Unexpected error")
                complete(StatusCodes.InternalServerError,
                         HttpEntity(ContentTypes.`application/json`,
                                    "{\"error\": true}"))
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 3000)

    println(s"Server online at http://localhost:3000/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
