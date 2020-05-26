package com.bchenault.association.service

import akka.stream.Materializer

import scala.concurrent.Future

class AssociationServiceImpl(materializer: Materializer) extends AssociationService {
  override def setAssociation(in: SetAssociationRequest): Future[SetAssociationResponse] = {
    Future.successful(SetAssociationResponse(in.input))
  }
}
