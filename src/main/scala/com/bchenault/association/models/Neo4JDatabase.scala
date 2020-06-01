package com.bchenault.association.models

import neotypes.GraphDatabase
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Neo4JDatabase @Inject()(implicit ec: ExecutionContext){
  val driver = GraphDatabase.driver[Future].apply("bolt://localhost:7687")
  val session = driver.session

    def dropAll(): Future[Unit] = {
      ???
    }
}
