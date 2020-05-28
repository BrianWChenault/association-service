package com.bchenault.association.services
import com.bchenault.association.models.{GraphEdge, GraphElement, Neo4JDatabase}
import com.bchenault.association.protobuf.{Association, Element}
import com.bchenault.association.utilities.FutureHelper
import gremlin.scala._
import javax.inject.{Inject, Singleton}
import cats.implicits._
import org.apache.tinkerpop.gremlin.structure.Direction

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Neo4JAssociationPersistence @Inject()(
                                           database: Neo4JDatabase
                                           )(implicit ec: ExecutionContext) extends AssociationPersistence {
  implicit val graph = database.fixture.graph

  override def createElement(protoElement: Element): Future[Element] = {
    createVertex(GraphElement.fromProto(protoElement))
      .map{ vertex =>
        vertex.toCC[GraphElement].toProto()
      }
  }

  override def getAssociationsFromId(fromId: String, edgeType: Option[String]): Future[Seq[Association]]= async {
    val edgeSelector = edgeType.toSeq
    await(getVertexById(fromId)) match {
      case Some(vertex) => {
        vertex.edges(Direction.BOTH, edgeSelector: _*)
          .asInstanceOf[Seq[Edge]]
          .map(edgeToAssociation)
      }
      case None => Seq.empty[Association]
    }
  }

  override def getAssociationsFromProperties(properties: Seq[String], edgeType: Option[String]): Future[Seq[Association]] = {
    val edgeSelector = edgeType.toSeq
    await(getVertexById(fromId)) match {
      case Some(vertex) => {
        vertex.edges(Direction.BOTH, edgeSelector: _*)
          .asInstanceOf[Seq[Edge]]
          .map(edgeToAssociation)
      }
      case None => Seq.empty[Association]
    }
  }

  override def setAssociation(from: Element, to: Element, edgeType: String): Future[Option[Association]] = {
    (for {
      fromVertex <- getOrCreateVertex(from)
      toVertex <- getOrCreateVertex(to)
    } yield {
      (fromVertex, toVertex) match {
        case (Some(parent), Some(child)) => Some(createAssociation(parent, child, GraphEdge(None, edgeType)))
        case _ => None
      }
    })
      .flatMap(_.traverse(_.map(edgeToAssociation)))
  }

  override def getElementById(elementId: String): Future[Option[Element]] = async {
    await(getVertexById(elementId))
      .map(_.toCC[GraphElement].toProto())
  }

  private def edgeToAssociation(edge: Edge): Association = {
    Association(
      fromElement = edge.outVertex().toCC[GraphElement].toProto().some,
      toElement = edge.inVertex().toCC[GraphElement].toProto().some,
      associationType = edge.label()
    )
  }

  private def getOrCreateVertex(protoElement: Element): Future[Option[Vertex]] = {
    protoElement.id match {
      case Some(id) => getVertexById(id)
      case None => createVertex(GraphElement.fromProto(protoElement)).map(_.some)
    }
  }

  private def getVertexById(id: String): Future[Option[Vertex]] = {
    def doGetVertex(id: String): Option[Vertex] = {
      graph.V.hasId(id).headOption()
    }
    FutureHelper.wrapMethod(doGetVertex, id)
  }

  private def getVerticesByProperties(properties: Seq[String]): Future[Seq[Vertex]] = {
    def doGetVertices(properties: Seq[String]): Seq[Vertex] = {
      graph.V.has
    }
  }

  private def createVertex(graphElement: GraphElement): Future[Vertex] = {
    def doCreateElement(element: GraphElement): Vertex = {
      val vertex = graph.addVertex(element)
      graph.tx().commit()
      vertex
    }
    FutureHelper.wrapMethod(doCreateElement, graphElement)
  }

  private def createAssociation(from: Vertex, to: Vertex, edge: GraphEdge): Future[Edge] = {
    case class SetRequest(from: Vertex, to: Vertex)
    def doSetAssociation(request: SetRequest): Edge = {
      val createdEdge = request.from --- GraphEdge(None, "") --> request.to
      graph.tx().commit()
      createdEdge
    }
    FutureHelper.wrapMethod(doSetAssociation, SetRequest(from, to))
  }
}
