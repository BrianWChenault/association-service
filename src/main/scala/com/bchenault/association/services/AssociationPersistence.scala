package com.bchenault.association.services

import com.bchenault.association.protobuf.{Association, Element}

import scala.concurrent.Future

trait AssociationPersistence {
  def createElement(element: Element): Future[Element]
  def getAssociationsFromId(fromId: String, edgeType: Option[String]): Future[Seq[Association]]
  def getAssociationsFromProperties(properties: Map[String, String], edgeType: Option[String]): Future[Seq[Association]]
  def setAssociation(from: Element, to: Element, edgeType: String): Future[Option[Association]]
  def getElementById(elementId: String): Future[Option[Element]]
}
