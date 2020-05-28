package com.bchenault.association.models

import com.steelbridgelabs.oss.neo4j.structure.{Neo4JGraphConfigurationBuilder, Neo4JGraphFactory}
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider
import com.bchenault.neoi4j.structure.providers.Neo4JNativeStringElementIdProvider
import gremlin.scala._
import javax.inject.Singleton

@Singleton
class Neo4JDatabase {
  val fixture = new {
    val configuration = Neo4JGraphConfigurationBuilder.connect(
      "localhost",
      7687.asInstanceOf[Short],
      "neo4j",
      "",
      "neo4j"
    ).withElementIdProvider(classOf[Neo4JNativeStringElementIdProvider])
      .withEdgeIdProvider(classOf[Neo4JNativeStringElementIdProvider])
      .build()

    implicit val graph = Neo4JGraphFactory.open(configuration).asScala()

    def cleanup(): Unit = {
      graph.V.drop().iterate()
      graph.tx().commit()
    }
  }

}
