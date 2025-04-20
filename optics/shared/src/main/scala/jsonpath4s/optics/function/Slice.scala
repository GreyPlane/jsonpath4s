package jsonpath4s.optics.function

import cats.Applicative
import cats.syntax.traverse.*
import monocle.Traversal

private def normalized(i: Int, length: Int): Int = {
  if (i >= 0) i else length + i
}

private def bounds(start: Int, end: Int, step: Int, length: Int): (Int, Int) = {
  val ns = normalized(start, length)
  val ne = normalized(end, length)

  if (step >= 0) {
    (
      ns.max(0).min(length),
      ne.max(0).min(length)
    )
  } else {
    (
      ne.max(-1).min(length - 1),
      ns.max(-1).min(length - 1)
    )
  }

}

// TODO find an elegant way to implement this
private[jsonpath4s] def slice[A](start: Option[Int], end: Option[Int], step: Option[Int]): Traversal[Vector[A], A] = {
  val stepValue = step.getOrElse(1)

  new Traversal[Vector[A], A] {
    def modifyA[F[_]: Applicative](f: A => F[A])(s: Vector[A]): F[Vector[A]] = {
      val length     = s.size
      val startValue = if (stepValue >= 0) start.getOrElse(0) else start.getOrElse(length - 1)
      val endValue   = if (stepValue > 0) end.getOrElse(length) else end.getOrElse(-length - 1)

      val (lower, upper) = bounds(startValue, endValue, stepValue, length)

      val result = scala.collection.mutable.ListBuffer.empty[A]

      if (stepValue > 0) {
        var i = lower
        while (i < upper) {
          result += s(i)
          i += stepValue
        }
      } else {
        var i = upper
        while (lower < i) {
          result += s(i)
          i += stepValue
        }
      }

      result.toVector.traverse(f)
    }
  }

}
