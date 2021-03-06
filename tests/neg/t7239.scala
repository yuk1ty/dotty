// Dotty rewrites only withFilter calls occurring in for expressions to filter calls.
// So this test does not compile.
object Test {
  def BrokenMethod(): HasFilter[(Int, String)] = ???

  trait HasFilter[B] {
    def filter(p: B => Boolean) = ???
  }

  trait HasWithFilter {
    def withFilter = ???
  }

  object addWithFilter {
    trait NoImplicit
    implicit def enrich(v: Any)
                       (implicit F0: NoImplicit): HasWithFilter = ???
  }

  BrokenMethod().withFilter(_ => true) // error // error
  BrokenMethod().filter(_ => true)     // ok

  locally {
    import addWithFilter._
    BrokenMethod().withFilter((_: (Int, String)) => true) // error
  }

  locally {
    import addWithFilter._
    // adaptToMemberWithArgs sets the type of the tree `x`
    // to ErrorType (while in silent mode, so the error is not
    // reported. Later, when the fallback from `withFilter`
    // to `filter` is attempted, the closure is taken to have
    // have the type `<error> => Boolean`, which conforms to
    // `(B => Boolean)`. Only later during pickling does the
    // defensive check for erroneous types in the tree pick up
    // the problem.
    BrokenMethod().withFilter(x => true) // error // error
  }
}
