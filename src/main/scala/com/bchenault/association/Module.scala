package com.bchenault.association

import akka.actor.ActorSystem
import com.bchenault.association.grpc.AssociationServer
import com.bchenault.association.services.{AssociationPersistence, Neo4JAssociationPersistence}
import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory

class Module() extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AssociationPersistence]).to(classOf[Neo4JAssociationPersistence])
  }
}
