package com.bchenault.association.grpc

import akka.actor.ActorSystem
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, Materializer}
import com.bchenault.association.protobuf.AssociationServiceHandler
import com.google.inject.{Guice, Inject}
import com.typesafe.config.ConfigFactory
import com.bchenault.association.Module
import com.bchenault.association.models.Neo4JDatabase
import com.bchenault.association.services.Neo4JAssociationPersistence

import scala.concurrent.{ExecutionContext, Future}

object AssociationServer {
  def runServer(args: Array[String]): Unit = {
    val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    implicit val system: ActorSystem = ActorSystem("association-service", conf)
    new AssociationServer(system).run()
  }
}

class AssociationServer @Inject() (system: ActorSystem) {

  def run(): Future[Http.ServerBinding] = {
    implicit val sys = system
    implicit val ec: ExecutionContext = system.dispatcher

    val database = new Neo4JDatabase()
    val persistence = new Neo4JAssociationPersistence(database)
    val serviceImpl = new AssociationServiceImpl(persistence)

    val service: HttpRequest => Future[HttpResponse] =
      AssociationServiceHandler(serviceImpl)

    val bound = Http().bindAndHandleAsync(
      service,
      interface = "127.0.0.1",
      port = 9001,
      connectionContext = HttpConnectionContext(http2 = Always)
    )

    bound.foreach { binding =>
      println(s"gRPC server bound to: ${binding.localAddress}")
    }

    bound
  }
}
