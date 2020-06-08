package com.bchenault.association

import com.bchenault.association.grpc.AssociationServer

object Main extends App {
  AssociationServer.runServer(Array.empty[String])
}
