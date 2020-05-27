//#full-example
package com.bchenault.assoiciations

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.bchenault.association.grpc.{AssociationServiceImpl, SetAssociationRequest, SetAssociationResponse}
import com.bchenault.associations.service.{SetAssociationRequest, SetAssociationResponse}
import com.bchenault.associations.{SetAssociationRequest, SetAssociationResponse}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span

class GreeterServiceImplSpec
  extends Matchers
  with WordSpecLike
  with BeforeAndAfterAll
  with ScalaFutures {

  implicit val patience = PatienceConfig(5.seconds, Span(100, org.scalatest.time.Millis))

  val system = ActorSystem("HelloWorldServer")
  implicit val mat = ActorMaterializer.create(system)
  val service = new AssociationServiceImpl(mat)

  override def afterAll: Unit = {
    Await.ready(system.terminate(), 5.seconds)
  }

  "GreeterServiceImpl" should {
    "reply to single request" in {
      val reply = service.setAssociation(SetAssociationRequest("foo"))
      reply.futureValue should === (SetAssociationResponse("foo"))
    }
  }
}
//#full-example
