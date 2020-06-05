package com.bchenault.association

import java.util.UUID

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.bchenault.association.grpc.AssociationServiceImpl
import com.bchenault.association.models.Neo4JDatabase
import com.bchenault.association.protobuf._
import com.bchenault.association.services.Neo4JAssociationPersistence
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers, WordSpecLike}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.time.Span
import cats.implicits._
import com.bchenault.association.protobuf.GetAssociationsRequest.FromSelector
import com.bchenault.association.protobuf.GetElementsRequest.ElementSelector

class AssociationServiceImplSpec
  extends FlatSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
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
    Await.ready(database.close(), 5.seconds)
    Await.ready(system.terminate(), 5.seconds)
  }

  override def beforeEach(): Unit = {
    Await.ready(database.dropAll(), 5.seconds)
  }

  "AssociationServiceImpl" should "create and query elements" in {
    val testName_0 = s"test_element_${UUID.randomUUID()}"
    val testName_1 = s"test_element_${UUID.randomUUID()}"
      whenReady(service.createElement(CreateElementRequest(name = testName_0, elementType = "test"))) { response_0 =>
        val elementId_0 = response_0.id.get

        whenReady(service.createElement(CreateElementRequest(name = testName_1, elementType = "test"))) { _ =>
          eventually {
            val getRequest = GetElementsRequest(
              elementSelector = ElementSelector.IdSelector(elementId_0)
            )
            whenReady(service.getElements(getRequest)) { getResponse =>
              getResponse.elements.size shouldBe 1
              getResponse.elements.find(_.id.get == elementId_0).get.name shouldBe testName_0
            }
          }
        }
      }
    }

    it should "create and query associations" in {
      val locationName = s"Location_${UUID.randomUUID()}"
      val personName = s"Person_${UUID.randomUUID()}"
      val setRequest = SetAssociationRequest(Association(
        fromElement = Element(
          name = locationName,
          elementType = "Location"
        ).some,
        toElement = Element(
          name = personName,
          elementType = "Person"
        ).some,
        associationType = "Resident"
      ).some)

      whenReady(service.setAssociation(setRequest)) { setResponse =>
        val getRequest = GetAssociationsRequest(
          fromSelector = FromSelector.IdSelector(setResponse.createdAssociation.get.fromElement.get.id.get),
          associationType = "Resident".some,
          returnAll = true
        )
        eventually {
          whenReady(service.getAssociations(getRequest)) { getResponse =>
            getResponse.totalSize shouldBe 1
            val association = getResponse.associations.head
            association.fromElement.get.name shouldBe locationName
            association.toElement.get.name shouldBe personName
          }
        }
      }
    }

  it should "support generic properties on elements" in {
    val elementName_0 = s"Element_${UUID.randomUUID()}"
    val elementName_1 = s"Element_${UUID.randomUUID()}"
    val createElementRequest_0 = CreateElementRequest(
      name = elementName_0,
      elementType = "person",
      properties = Map[String, String]("age" -> "28", "sign" -> "aries", "hair" -> "blond", "gender" -> "male")
    )
    val createElementRequest_1 = CreateElementRequest(
      name = elementName_1,
      elementType = "person",
      properties = Map[String, String]("age" -> "32", "sign" -> "gemini", "hair" -> "brown", "gender" -> "male")
    )

    whenReady(service.createElement(createElementRequest_0)) { _ =>
      whenReady(service.createElement(createElementRequest_1)) { _ =>
        val getRequest_0 = GetElementsRequest(
          elementSelector = ElementSelector.PropertySelector(PropertySelector(Map[String, String]("gender" -> "male")))
        )
        val getRequest_1 = GetElementsRequest(
          elementSelector = ElementSelector.PropertySelector(PropertySelector(Map[String, String]("sign" -> "aries")))
        )

        eventually {
          whenReady(service.getElements(getRequest_0)) { response =>
            response.elements.size shouldBe 2
            response.elements.map(_.name) should contain theSameElementsAs Seq(elementName_0, elementName_1)
          }

          whenReady(service.getElements(getRequest_1)) { response =>
            response.elements.size shouldBe 1
            response.elements.map(_.name) should contain theSameElementsAs Seq(elementName_0)
          }
        }
      }
    }
  }

}
