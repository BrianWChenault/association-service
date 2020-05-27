package com.bchenault.association.models

import com.steelbridgelabs.oss.neo4j.structure.{Neo4JGraphConfigurationBuilder, Neo4JGraphFactory}
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider
import gremlin.scala._

class Neo4JDatabase {
  val fixture = new {
    val configuration = Neo4JGraphConfigurationBuilder.connect(
      "localhost",
      7687.asInstanceOf[Short],
      "neo4j",
      "",
      "neo4j"
    ).withElementIdProvider(classOf[Neo4JNativeElementIdProvider])
      .withEdgeIdProvider(classOf[Neo4JNativeElementIdProvider])
      .build()

    implicit val graph = Neo4JGraphFactory.open(configuration).asScala()

    def cleanup(): Unit = {
      graph.V.drop().iterate()
      graph.tx().commit()
    }
  }

}
