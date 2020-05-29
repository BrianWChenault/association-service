package com.bchenault.association.grpc

import akka.stream.Materializer
import com.bchenault.association.protobuf.GetAssociationsRequest.FromSelector
import com.bchenault.association.protobuf._
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

  override def getElements(request: GetElementsRequest): Future[GetElementsResponse] = {
    Future.sequence(request.ids.map { elementId =>
      associationPersistence.getElementById(elementId)
    })
      .map(allElements => GetElementsResponse(elements = allElements.flatten))
  }

  override def getAssociations(request: GetAssociationsRequest): Future[GetAssociationsResponse] = {
    val associations = request.fromSelector match {
      case FromSelector.Empty =>
        Future.successful(Seq.empty[Association])
      case FromSelector.IdSelector(elementId) =>
        associationPersistence.getAssociationsFromId(elementId, request.associationType)
      case FromSelector.PropertySelector(propertySelector) =>
        associationPersistence.getAssociationsFromProperties(propertySelector.properties, request.associationType)
    }

    associations
      .map(handlePagination(_, request.returnAll, request.firstResult, request.resultCount))
        .map(results => GetAssociationsResponse(
          associations = results,
          totalSize = results.size
        ))
  }

  private def handlePagination[T](elements: Seq[T], returnAll: Boolean, firstResult: Int, resultCount: Int): Seq[T] = {
    if (returnAll) {
      elements
    } else {
      elements.slice(firstResult, resultCount)
    }
  }
}
