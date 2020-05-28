package com.bchenault.association

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.bchenault.association.grpc.AssociationServiceImpl
import com.bchenault.association.models.Neo4JDatabase
import com.bchenault.association.protobuf._
import com.bchenault.association.services.Neo4JAssociationPersistence
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers, WordSpecLike}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.time.Span

class AssociationServiceImplSpec
  extends FlatSpec
  with Matchers
  with BeforeAndAfterAll
  with Eventually
  with ScalaFutures {

  implicit val patience = PatienceConfig(5.seconds, Span(100, org.scalatest.time.Millis))

  val system = ActorSystem("association-service")
  implicit val mat = ActorMaterializer.create(system)
  implicit val ec = ExecutionContext.global
  val database = new Neo4JDatabase()
  val persistence = new Neo4JAssociationPersistence(database)
  val service = new AssociationServiceImpl(persistence, mat)

  override def afterAll: Unit = {
    Await.ready(system.terminate(), 5.seconds)
  }

  override def beforeAll: Unit = {
    database.fixture.cleanup()
  }

  "AssociationServiceImpl" should "create and query elements" in {
      whenReady(service.createElement(CreateElementRequest(name = "test_element_0", elementType = "test"))) { response_0 =>
        val elementId_0 = response_0.id.get

        whenReady(service.createElement(CreateElementRequest(name = "test_element_1", elementType = "test"))) { response_1 =>
          val elementId_1 = response_1.id.get

          eventually {
            whenReady(service.getElements(GetElementsRequest(ids = Seq(elementId_0, elementId_1)))) { getResponse =>
              getResponse.elements.size shouldBe 2
              getResponse.elements.find(_.id.get == elementId_0).get.name shouldBe "test_element_0"
              getResponse.elements.find(_.id.get == elementId_1).get.name shouldBe "test_element_1"
            }
          }
        }
      }
    }

    it should "create and query associations" in {

    }

}
