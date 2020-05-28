package com.bchenault.association.models

import com.bchenault.association.protobuf.Element
import gremlin.scala.{id, label}

@label("element")
case class GraphElement(@id uuid: Option[String], elementType: String, name: String) extends DatabaseEntity {
  def toProto(): Element = Element(
    id = uuid,
    name = name,
    elementType = elementType
  )
}

object GraphElement {
  def fromProto(protoElement: Element): GraphElement = GraphElement(
     uuid = protoElement.id,
    elementType = protoElement.elementType,
    name = protoElement.name
    )
}
