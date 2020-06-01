package com.bchenault.association.services
import java.util.UUID

import cats.implicits._
import com.bchenault.association.models.{GraphElement, Neo4JDatabase}
import com.bchenault.association.protobuf.{Association, Element}
import javax.inject.{Inject, Singleton}
import neotypes.implicits.all._
import org.neo4j.driver.v1.summary.ResultSummary

import scala.async.Async._
import scala.collection.immutable.ListMap
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
    val selector = s"elementId: '$fromId'"
    getAssociationAction(selector, edgeType.getOrElse("")).map(_.toSeq)
  }

  override def getAssociationsFromProperties(properties: Map[String, String], edgeType: Option[String]): Future[Seq[Association]] = {
    val selector = constructPropertySelector(properties)
    getAssociationAction(selector, edgeType.getOrElse("")).map(_.toSeq)
  }

  override def setAssociation(fromElement: Element, toElement: Element, edgeType: String): Future[Option[Association]] = async {
    val from = await(getOrCreateElement(fromElement))
    val to = await(getOrCreateElement(toElement))
    val createOperation = (from.flatMap(_.id), to.flatMap(_.id)) match {
      case (Some(fromId), Some(toId)) => createAssociationAction(fromId, toId, edgeType).map(_.some)
      case _ => Future.successful(none[Unit])
    }
    await(createOperation).map { _ => Association(from, to, edgeType)}
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

  private def createAssociationAction(fromId: String, toId: String, edgeType: String): Future[Association] = async {
    case class EdgeTest(edgeType: String)
    val createQuery = c"MATCH (from:element { elementId: $fromId }),(to:element { elementId: $toId }) " +
      c"CREATE (from)-[r:ASSOCIATION { edgeType: $edgeType } ]->(to)" +
      c"RETURN from,to"
    val joinedElements = await(createQuery.query[(GraphElement, GraphElement)].single(database.session))
    Association(
      fromElement = joinedElements._1.toProto().some,
      toElement =joinedElements._2.toProto().some,
      associationType = edgeType
    )
  }

  private def getAssociationAction(fromSelector: String, edgeType: String): Future[List[Association]] = async {
    val searchQuery =
      s"MATCH (from:element { $fromSelector })-[a:ASSOCIATION{ edgeType: '$edgeType' }]->(to:element) " +
        s"RETURN from,to"
    await(searchQuery.query[(GraphElement, GraphElement)].list(database.session))
      .map { elementPair =>
        Association(
          fromElement = elementPair._1.toProto().some,
          toElement = elementPair._2.toProto().some,
          associationType = edgeType
        )
      }
  }

  private def getElementByIdAction(id: String): Future[Option[Element]] = database.driver.readSession { session =>
    c"MATCH (e: element { elementId: $id }) RETURN e"
      .query[Option[GraphElement]]
      .single(session)
      .map(_.map(_.toProto()))
  }

  private def createElementAction(element: Element): Future[Element] = {
    val uuid = element.id.getOrElse(generateId())
    c"MERGE (e: element { elementId: $uuid, name: ${element.name}, elementType: ${element.elementType}}) RETURN e"
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
