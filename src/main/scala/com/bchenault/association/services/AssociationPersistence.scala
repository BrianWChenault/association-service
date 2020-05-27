package com.bchenault.association.services

import com.bchenault.association.protobuf.Element

import scala.concurrent.Future

trait AssociationPersistence {
  def createElement(element: Element): Future[Element]
  def setChildToParentAssociation(parentId: String, childElement: Element): Future[Boolean]
  def getChildrenAssociations(id: String, childTypes: Seq[String]): Future[Seq[Element]]
  def getParentAssociation(id: String): Future[Option[Element]]
}
