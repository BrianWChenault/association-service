package com.bchenault.association.models

import gremlin.scala.{id, label}

@label("association")
case class GraphEdge(@id uuid: Option[String], edgeType: String) extends DatabaseEntity

