package jsonpath4s.optics.function

import cats.Applicative
import monocle.Traversal
import cats.implicits.given

trait Plated {

  def cosmos[A](plate: Traversal[A, A]): Traversal[A, A] = new Traversal[A, A]:
    def modifyA[F[_]: Applicative](f: A => F[A])(s: A): F[A] = {
      f(s).productR(plate.modifyA(cosmos(plate).modifyA(f))(s))
    }

}
