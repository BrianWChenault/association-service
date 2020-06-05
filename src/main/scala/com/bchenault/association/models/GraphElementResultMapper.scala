package com.bchenault.association.models

import neotypes.mappers
import neotypes.mappers.ResultMapper
import org.neo4j.driver.internal.value.NodeValue
import org.neo4j.driver.v1.Value
import cats.implicits._

object GraphElementResultMapper {
  def getMapper: ResultMapper[GraphElement] = new ResultMapper[GraphElement] {
    override def to(value: List[(String, Value)], typeHint: Option[mappers.TypeHint]): Either[Throwable, GraphElement] = {
      val mappedElements = value.map { labelToElement =>
        val elementProperties = labelToElement._2.asInstanceOf[NodeValue].asMap[String](_.toString).entrySet()
        var elementId = none[String]
        var elementType = ""
        var name = ""
        var additionalProperties = Map[String, String]()
        elementProperties.forEach { entry =>
          entry.getKey match {
            case "elementId" => elementId = entry.getValue.some
            case "elementType" => elementType = entry.getValue
            case "name" => name = entry.getValue
            case _ => additionalProperties = additionalProperties.updated(entry.getKey, entry.getValue)
          }
        }

        GraphElement(elementId, elementType, name, additionalProperties)
      }
      println(mappedElements)

      mappedElements.headOption match {
        case Some(value) => Right(value)
        case None => Left(new RuntimeException("Something happened in query"))
      }
    }
  }
}
