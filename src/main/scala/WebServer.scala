import PaymentClient.CardException
import PurchaseService.Order
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.{Failure, Success}

object WebServer {

  final case class OrderRequest(cardNumber: String)

  def main(args: Array[String]) = {

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher

    implicit val orderFormat: RootJsonFormat[Order] = jsonFormat1(Order)
    implicit val orderRequestFormat: RootJsonFormat[OrderRequest] = jsonFormat1(
      OrderRequest)

    val route =
      path("order") {
        post {
          entity(as[OrderRequest]) { orderRequest =>
            onComplete(PurchaseService.createPurchase(orderRequest)) {
              case Success(result) =>
                complete(StatusCodes.Created, result)
              case Failure(exception) =>
                exception match {
                  case e: CardException =>
                    complete(
                      StatusCodes.UnprocessableEntity,
                      HttpEntity(
                        ContentTypes.`application/json`,
                        "{\"message\": \"Invalid card\", \"code\": \"" + e.declineCode + "\"}"))

                  case _ =>
                    system.log.error(exception, "Unexpected error")
                    complete(
                      StatusCodes.InternalServerError,
                      HttpEntity(ContentTypes.`application/json`,
                                 "{\"message\": \"Something went wrong\"}"))
                }
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
