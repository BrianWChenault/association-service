package com.bchenault.association.grpc

import java.util.UUID

import akka.stream.Materializer
import com.bchenault.association.protobuf.{AssociationService, CreateElementRequest, CreateElementResponse, Element, SetAssociationRequest, SetAssociationResponse}
import com.bchenault.association.services.AssociationPersistence
import io.grpc.{Status, StatusException}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class AssociationServiceImpl @Inject()(
                                      associationPersistence: AssociationPersistence,
                                        materializer: Materializer
                                      )(implicit ex: ExecutionContext) extends AssociationService {
  override def setAssociation(request: SetAssociationRequest): Future[SetAssociationResponse] = {
    request.child.map { child =>
      associationPersistence.setAssociation(request.parentId, child)
        .map(_ => SetAssociationResponse())
    }.getOrElse{
      throw new StatusException(Status.INVALID_ARGUMENT)
    }
  }

  override def createElement(request: CreateElementRequest): Future[CreateElementResponse] = {
    val elementId = UUID.randomUUID().toString
    associationPersistence.createElement(
      Element(id = elementId, name = request.name, elementType = request.elementType)
    )
      .map(_ => CreateElementResponse(id = elementId))
  }
}
