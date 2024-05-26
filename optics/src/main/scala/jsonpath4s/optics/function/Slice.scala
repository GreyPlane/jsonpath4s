package jsonpath4s.optics.function

import cats.implicits.given
import cats.{Applicative, Traverse}
import monocle.Traversal

private def normalized(i: Int, length: Int): Int = {
  if (i >= 0) i else length + i
}

private def bounds(start: Int, end: Int, step: Int, length: Int): (Int, Int) = {
  val normalizedStart = normalized(start, length)
  val normalizedEnd   = normalized(end, length)

  if (step >= 0) {
    (
      normalizedStart.max(0).min(length),
      normalizedEnd.max(0).min(length)
    )
  } else {
    (
      normalizedStart.max(-1).min(length - 1),
      normalizedEnd.max(-1).min(length - 1)
    )
  }

}

private def range(step: Int, lower: Int, upper: Int) = {
  if (step > 0) {
    Range(lower, upper, step)
  } else if (step < 0) {
    Range(upper, lower, step)
  } else {
    Range(0, 0)
  }
}

def slice[A, T[_]: Traverse](start: Option[Int], end: Option[Int], step: Option[Int]): Traversal[T[A], A] = {
  val stepValue = step.getOrElse(1)

  new Traversal[T[A], A] {
    def modifyA[F[_]: Applicative](f: A => F[A])(s: T[A]): F[T[A]] = {
      val length     = s.foldLeft(0)((n, _) => n + 1)
      val startValue = if (stepValue >= 0) start.getOrElse(0) else start.getOrElse(length - 1)
      val endValue   = if (stepValue > 0) end.getOrElse(length) else end.getOrElse(-length - 1)

      val (lower, upper) = bounds(startValue, endValue, stepValue, length)

      val indices = range(stepValue, lower, upper).toSet

      s.zipWithIndex.traverse { case (a, i) =>
        if (indices.contains(i)) f(a) else a.pure[F]
      }
    }
  }

}
