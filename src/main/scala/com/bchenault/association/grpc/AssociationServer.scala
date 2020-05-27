package com.bchenault.association.grpc

import akka.actor.ActorSystem
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, Materializer}
import com.bchenault.association.protobuf.AssociationServiceHandler
import com.google.inject.Inject
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

//object AssociationServer {
//
//  def main(args: Array[String]): Unit = {
//    val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
//      .withFallback(ConfigFactory.defaultApplication())
//    val system: ActorSystem = ActorSystem("association-service", conf)
//    new AssociationServer(system).run()
//  }
//}

class AssociationServer @Inject() (serviceImpl: AssociationServiceImpl) {
  val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())
  implicit val system: ActorSystem = ActorSystem("association-service", conf)

  def run(): Future[Http.ServerBinding] = {
    implicit val mat: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val service: HttpRequest => Future[HttpResponse] =
      AssociationServiceHandler(serviceImpl)

    val bound = Http().bindAndHandleAsync(
      service,
      interface = "127.0.0.1",
      port = 8080,
      connectionContext = HttpConnectionContext(http2 = Always)
    )

    bound.foreach { binding =>
      println(s"gRPC server bound to: ${binding.localAddress}")
    }

    bound
  }

  run()
}
