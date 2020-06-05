package com.bchenault.association.grpc

import akka.stream.Materializer
import com.bchenault.association.protobuf.GetAssociationsRequest.FromSelector
import com.bchenault.association.protobuf.GetElementsRequest.ElementSelector
import com.bchenault.association.protobuf._
import com.bchenault.association.services.AssociationPersistence
import io.grpc.{Status, StatusException}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class AssociationServiceImpl @Inject()(
                                      associationPersistence: AssociationPersistence
                                      )(implicit ex: ExecutionContext, materializer: Materializer) extends AssociationService {
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
      Element(id = None, name = request.name, elementType = request.elementType, properties = request.properties)
    )
      .map(createdElement => CreateElementResponse(id = createdElement.id))
  }

  override def getElements(request: GetElementsRequest): Future[GetElementsResponse] = {
    val allElements = request.elementSelector match {
      case ElementSelector.Empty => Future.successful(Seq.empty[Element])
      case ElementSelector.IdSelector(id) => associationPersistence.getElementById(id).map(_.toSeq)
      case ElementSelector.PropertySelector(propertySelector) =>
        associationPersistence.getElementsFromProperties(propertySelector.properties)
    }
    allElements.map(GetElementsResponse(_))
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
