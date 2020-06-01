package com.bchenault.association.services
import java.util.UUID

import cats.implicits._
import com.bchenault.association.models.{GraphElement, Neo4JDatabase}
import com.bchenault.association.protobuf.{Association, Element}
import javax.inject.{Inject, Singleton}
import neotypes.implicits.all._

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Neo4JAssociationPersistence @Inject()(
                                           database: Neo4JDatabase
                                           )(implicit ec: ExecutionContext) extends AssociationPersistence {
  override def createElement(element: Element): Future[Element] = {
    (for {
      existingElement <- getElementsByPropertiesAction(getPropertyMap(element)).map(_.headOption)
    } yield {
      existingElement match {
       case Some(existingElement) => Future.successful(existingElement)
        case None => createElementAction(element)
      }
    }).flatten
  }

  override def getAssociationsFromId(fromId: String, edgeType: Option[String]): Future[Seq[Association]]= {
    val selector = s"id: '$fromId'"
    getAssociationAction(selector, edgeType.getOrElse("")).map(_.toSeq)
  }

  override def getAssociationsFromProperties(properties: Map[String, String], edgeType: Option[String]): Future[Seq[Association]] = {
    val selector = constructPropertySelector(properties)
    getAssociationAction(selector, edgeType.getOrElse("")).map(_.toSeq)
  }

  override def setAssociation(from: Element, to: Element, edgeType: String): Future[Option[Association]] = async {
    val fromId = await(getOrCreateElement(from)).flatMap(_.id)
    val toId = await(getOrCreateElement(to)).flatMap(_.id)
    val association = (fromId, toId) match {
      case (Some(parentId), Some(childId)) => createAssociationAction(parentId, childId, edgeType)
      case _ => Future.successful(none[Association])
    }
    await(association)
  }

  override def getElementById(elementId: String): Future[Option[Element]] = {
    getElementByIdAction(elementId)
  }

  private def getOrCreateElement(protoElement: Element): Future[Option[Element]] = {
    protoElement.id match {
      case Some(id) => getElementById(id)
      case None => createElement(protoElement).map(_.some)
    }
  }

  private def createAssociationAction(fromId: String, toId: String, edgeType: String): Future[Option[Association]] = async {
    val createQuery = c"MATCH (from:element),(to:element) " +
      c"WHERE from.id = '$fromId' AND to.id = '$fromId' " +
      c"CREATE (from)-[r:association { edgeType: $edgeType }]->(to) " +
      c"RETURN from, to"
    await(createQuery.query[(GraphElement, GraphElement)].set(database.session))
      .headOption.map { joinedElements =>
      Association(
        fromElement = joinedElements._1.toProto().some,
        toElement = joinedElements._2.toProto().some,
        associationType = edgeType
      )
    }
  }

  private def getAssociationAction(fromSelector: String, edgeType: String): Future[Set[Association]] = async {
    val searchQuery = s"MATCH (from:element { $fromSelector })-[a:association { edgeType: $edgeType }]->(to:element) " +
      s"RETURN from, to"
    await(searchQuery.query[(GraphElement, GraphElement)].set(database.session))
      .map { elementPair =>
        Association(
          fromElement = elementPair._1.toProto().some,
          toElement = elementPair._2.toProto().some,
          associationType = edgeType
        )
      }
  }

  private def getElementByIdAction(id: String): Future[Option[Element]] = database.driver.readSession { session =>
    c"MATCH (e: element { id: $id }) RETURN e"
      .query[Option[GraphElement]]
      .single(session)
      .map(_.map(_.toProto()))
  }

  private def createElementAction(element: Element): Future[Element] = {
    val uuid = element.id.getOrElse(generateId())
    c"MERGE (e: element { id: $uuid, name: ${element.name}, elementType: ${element.elementType}}) RETURN e"
      .query[GraphElement]
      .single(database.session)
      .map(_.toProto())
  }

  private def generateId(): String = UUID.randomUUID().toString

  private def getElementsByPropertiesAction(properties: Map[String, String]): Future[Seq[Element]] = database.driver.readSession { session =>
    val propertySelector = constructPropertySelector(properties)
    s"MATCH (e: element { $propertySelector }) RETURN e"
      .query[GraphElement]
      .list(session)
      .map(_.map(_.toProto()))
  }

  private def getPropertyMap(protoElement: Element): Map[String, String] =
    Map("name" -> protoElement.name, "elementType" -> protoElement.elementType)

  private def constructPropertySelector(properties: Map[String, String]): String =
  properties.foldLeft(Seq.empty[String]) { (agg, next) =>
    agg :+ s"${next._1}: '${next._2}'"
  }.mkString(", ")

}
