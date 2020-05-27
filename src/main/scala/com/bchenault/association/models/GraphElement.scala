package com.bchenault.association.models

import com.bchenault.association.protobuf.Element
import gremlin.scala.{id, label}

@label("element")
case class GraphElement(@id id: Option[String], elementType: String, name: String) {
  def toProto(): Element = Element(
    id = id,
    name = name,
    elementType = elementType
  )
}

object GraphElement {
  def fromProto(protoElement: Element): GraphElement = GraphElement(
     id = protoElement.id,
    elementType = protoElement.elementType,
    name = protoElement.name
    )
}
