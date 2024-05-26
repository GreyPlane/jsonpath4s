package jsonpath4s.optics.function

import cats.{Traverse, Applicative}
import monocle.{Traversal, Fold}
import cats.implicits.given

import cats.Monoid

def combineAll[A, T[_]: Traverse: Applicative](traversals: T[Traversal[A, A]]): Fold[A, A] = new Fold[A, A]:
  def foldMap[M: Monoid](f: A => M)(s: A): M = {
    Applicative[T].map(traversals)(_.foldMap(f)(s)).foldLeft(Monoid[M].empty)(_ combine _)
  }
