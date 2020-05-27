package com.bchenault.association.utilities

import scala.concurrent.{Future, Promise}
import scala.util.Try

object FutureHelper {
  def wrapMethod[Request, Result](
                                           f:       Request => Result,
                                           request: Request
                                         ): Future[Result] = {
    val p = Promise[Result]
    p complete {
      Try {
        f(request)
      }
    }
    p.future
  }

}
