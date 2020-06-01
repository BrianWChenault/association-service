package com.bchenault.association.models

import com.bchenault.association.protobuf.Association

case class GraphEdge(from: GraphElement, to: GraphElement, edgeType: String) {
  def toAssociation = Association(

  )
}

