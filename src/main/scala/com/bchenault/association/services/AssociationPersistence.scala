package com.bchenault.association.services

import com.bchenault.association.protobuf.Element

import scala.concurrent.Future

trait AssociationPersistence {
  def createElement(element: Element): Future[Unit]
  def setAssociation(parentId: String, childElement: Element): Future[Unit]
  def getChildrenAssociations(id: String, childTypes: Seq[String]): Future[Seq[Element]]
  def getParentAssociations(id: String): Future[Option[Element]]
}
