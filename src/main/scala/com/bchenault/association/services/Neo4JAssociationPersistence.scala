package com.bchenault.association.services
import com.bchenault.association.models.{GraphElement, Neo4JDatabase}
import com.bchenault.association.protobuf.Element
import com.bchenault.association.utilities.FutureHelper
import gremlin.scala.{id, label}
import javax.inject.Inject
import org.apache.tinkerpop.gremlin.structure.Vertex

import scala.concurrent.{ExecutionContext, Future}

class Neo4JAssociationPersistence @Inject()(
                                           database: Neo4JDatabase
                                           )(implicit ec: ExecutionContext) extends AssociationPersistence {

  implicit val graph = database.fixture.graph

  override def getChildrenAssociations(id: String, childTypes: Seq[String]): Future[Seq[Element]] = ???

  override def getParentAssociations(id: String): Future[Option[Element]] = ???

  override def createElement(protoElement: Element): Future[Unit] = {
    def doCreateElement(element: GraphElement): Vertex = {
      val vertex = graph.addVertex(element)
      graph.tx().commit()
      vertex
    }
    FutureHelper.wrapMethod(doCreateElement, GraphElement.fromProto(protoElement))
  }

  override def setAssociation(parentId: String, childElement: Element): Future[Unit] = {
    case class SetRequest(parent: Vertex, child: Vertex)
    def doSetAssociation(request: SetRequest): GraphElement = {
      val edge =
    }
    for {
      parentVertex <- getVertexById(parentId)
    }
    FutureHelper.wrapMethod(doSetAssociation, SetRequest(pare))
  }

  private def getVertexById(id: String): Future[Option[Vertex]] = {
    def doGetVertex(id: String): Option[Vertex] = graph.V(id).headOption()
    FutureHelper.wrapMethod(doGetVertex, id)
  }
}
