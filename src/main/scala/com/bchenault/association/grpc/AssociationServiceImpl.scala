package com.bchenault.association.grpc

import akka.stream.Materializer
import com.bchenault.association.protobuf.{Association, AssociationService, CreateElementRequest, CreateElementResponse, Element, GetElementRequest, GetElementResponse, SetAssociationRequest, SetAssociationResponse}
import com.bchenault.association.services.AssociationPersistence
import io.grpc.{Status, StatusException}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AssociationServiceImpl @Inject()(
                                      associationPersistence: AssociationPersistence,
                                        materializer: Materializer
                                      )(implicit ex: ExecutionContext) extends AssociationService {
  override def setAssociation(request: SetAssociationRequest): Future[SetAssociationResponse] = {
    val association = request.associationRequest.getOrElse(Association())
    (for {
      from <- association.fromElement
      to <- association.toElement
    } yield {
      associationPersistence.setAssociation(from, to, association.associationType)
          .map(createdAssociation => SetAssociationResponse(createdAssociation))
    })
      .getOrElse{
        throw new StatusException(Status.INVALID_ARGUMENT)
      }
  }

  override def createElement(request: CreateElementRequest): Future[CreateElementResponse] = {
    associationPersistence.createElement(
      Element(id = None, name = request.name, elementType = request.elementType)
    )
      .map(createdElement => CreateElementResponse(id = createdElement.id))
  }

  override def getElement(request: GetElementRequest): Future[GetElementResponse] = {
    Future.sequence(request.ids.map { elementId =>
      associationPersistence.getElementById(elementId)
    })
      .map(allElements => GetElementResponse(elements = allElements.flatten))
  }
}
