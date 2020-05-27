package com.bchenault.association.services

import com.bchenault.association.protobuf.{Association, Element}

import scala.concurrent.Future

trait AssociationPersistence {
  def createElement(element: Element): Future[Element]
  def getAssociationsByEdgeType(elementId: String, edgeType: String): Future[Seq[Association]]
  def setAssociation(from: Element, to: Element, edgeType: String): Future[Option[Association]]
}
