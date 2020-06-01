package com.bchenault.association.models

import com.bchenault.association.protobuf.Element

case class GraphElement(uuid: Option[String], elementType: String, name: String) {
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
