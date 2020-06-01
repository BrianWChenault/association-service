package com.bchenault.association.models

import com.bchenault.association.protobuf.Element

case class GraphElement(elementId: Option[String], elementType: String, name: String) {
  def toProto(): Element = Element(
    id = elementId,
    name = name,
    elementType = elementType
  )
}

object GraphElement {
  def fromProto(protoElement: Element): GraphElement = GraphElement(
     elementId = protoElement.id,
    elementType = protoElement.elementType,
    name = protoElement.name
    )
}
