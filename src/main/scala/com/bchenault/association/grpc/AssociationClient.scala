package com.bchenault.association.grpc

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import com.bchenault.association.protobuf.{AssociationServiceClient, SetAssociationRequest}

import scala.util.{Failure, Success}

object AssociationClient {

  def runClient(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("association-service")
    implicit val ec = sys.dispatcher

    val client = AssociationServiceClient(GrpcClientSettings.fromConfig("associations.AssociationService"))

    def singleRequestReply(name: String): Unit = {
      println(s"Performing request: $name")
      val reply = client.setAssociation(SetAssociationRequest())
      reply.onComplete {
        case Success(msg) =>
          println(msg)
        case Failure(e) =>
          println(s"Error: $e")
      }
    }
  }
}
