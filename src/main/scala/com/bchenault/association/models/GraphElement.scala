package com.bchenault.association.models

import com.bchenault.association.protobuf.Element

case class GraphElement(elementId: Option[String], elementType: String, name: String, properties: Map[String, String]) {
  def toProto(): Element = Element(
    id = elementId,
    name = name,
    elementType = elementType,
    properties = properties
  )
}

object GraphElement {
  def fromProto(protoElement: Element): GraphElement = GraphElement(
     elementId = protoElement.id,
    elementType = protoElement.elementType,
    name = protoElement.name,
    properties = protoElement.properties
    )
}

