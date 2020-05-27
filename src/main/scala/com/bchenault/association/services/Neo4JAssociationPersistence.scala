package com.bchenault.association.services
import com.bchenault.association.models.{GraphEdge, GraphElement, Neo4JDatabase}
import com.bchenault.association.protobuf.Element
import com.bchenault.association.utilities.FutureHelper
import gremlin.scala._
import javax.inject.Inject
import cats.implicits._
import org.apache.tinkerpop.gremlin.structure.Direction

import scala.concurrent.{ExecutionContext, Future}

class Neo4JAssociationPersistence @Inject()(
                                           database: Neo4JDatabase
                                           )(implicit ec: ExecutionContext) extends AssociationPersistence {
  implicit val graph = database.fixture.graph

  override def getChildrenAssociations(id: String, childTypes: Seq[String]): Future[Seq[Element]] = ???

  override def getParentAssociation(id: String): Future[Option[Element]] = {
    getVertexById(id).map {
        case Some(vertex) => {
          vertex.asScala().out()

        }
        case None => None
    }
    ???
  }

  override def createElement(protoElement: Element): Future[Element] = {
    FutureHelper.wrapMethod(doCreateElement, GraphElement.fromProto(protoElement))
      .map(vertex => vertex.asInstanceOf[GraphElement].toProto())
  }

  override def setChildToParentAssociation(parentId: String, childElement: Element): Future[Boolean] = {
    (for {
      parentVertex <- getVertexById(parentId)
      childVertex <- FutureHelper.wrapMethod(doCreateElement, GraphElement.fromProto(childElement))
    } yield {
      parentVertex.traverse(parent => createAssociation(childVertex, parent))
    })
      .flatten
      .map(_.nonEmpty)
  }

  private def getVertexById(id: String): Future[Option[Vertex]] = {
    def doGetVertex(id: String): Option[Vertex] = graph.V(id).headOption()
    FutureHelper.wrapMethod(doGetVertex, id)
  }

  private def doCreateElement(element: GraphElement): Vertex = {
    val vertex = graph.addVertex(element)
    graph.tx().commit()
    vertex
  }

  private def createAssociation(from: Vertex, to: Vertex): Future[Edge] = {
    case class SetRequest(from: Vertex, to: Vertex)
    def doSetAssociation(request: SetRequest): Edge = {
      val createdEdge = request.from --- GraphEdge(None) --> request.to
      graph.tx().commit()
      createdEdge
    }
    FutureHelper.wrapMethod(doSetAssociation, SetRequest(from, to))
  }
}
