package com.bchenault.association.models

import neotypes.GraphDatabase
import javax.inject.{Inject, Singleton}
import neotypes.implicits.all._
import org.neo4j.driver.v1.summary.ResultSummary

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Neo4JDatabase @Inject()(implicit ec: ExecutionContext){
  val driver = GraphDatabase.driver[Future].apply("bolt://localhost:7687")
  val session = driver.session

    def close(): Future[Unit] = {
      session.close
      driver.close
    }

  def dropAll() : Future[Unit] = {
    c"MATCH (n) DETACH DELETE n"
      .query[ResultSummary]
      .execute(session)
      .map(_ => {})
  }
}
