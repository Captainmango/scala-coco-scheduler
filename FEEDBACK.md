# Code review feedback

Focus is on overall construction and idiomatic Scala, not formatting.

## What you're doing well

* **Clear separation of concerns.** You split the problem into Lexer → Validator → Evaluator → Formatter. That pipeline is easy to follow and test.
* **Good use of Scala 3 ADTs.** `CronInterval` as an `enum` with bounds, a `sealed trait` for fragments, and case classes for the variants are solid modelling choices.
* **Tests exist for each phase.** Having unit tests for the lexer, validator and evaluator means you can refactor each stage independently with confidence.
* **Build tooling is sensible.** Scala 3.3.8, munit, mainargs, plus scalafmt/scalafix on compile is a good setup for learning.
* **Pattern matching is appropriate.** You reach for `match` rather than `isInstanceOf` chains in most places.

## Where the tagless-final design isn't paying off yet

The scaffold is there (`CronParserAlg`, `CronEither`, `Monadish`), but it isn't actually being used in an idiomatic tagless-final way.

### `Monadish` is defined but never used

`Monadish[F[_]]` has a `given MyMonad: Monadish[CronEither]`, but nothing in the program asks for a `Monadish` instance. In tagless-final, the business logic is usually written abstractly:

```scala
def program[F[_]: Monad](parser: CronParserAlg[F]): F[Cron] =
  parser.parse("*/15 0 1,15 * 1-5")
```

Right now your `parse` implementation is concrete `Either` code with early `return`s, so the type class is dead weight.

### The algebra instance hardcodes `Either`

```scala
given CronParser: CronParserAlg[CronEither] with {
  override def parse(input: String): CronEither[Cron] = {
    val lexer = Lexer.make(input)
    val acl = lexer.lex() match {
      case Right(v)        => v
      case l @ Left(value) => return l.asInstanceOf[CronEither[Cron]]
    }
    ...
  }
}
```

This only gives you one interpreter: `Either[ParserError, *]`. The value of tagless-final is being able to plug in other interpretations (e.g. a test interpreter, a traced interpreter, an effectful one). If you only ever need `Either`, a plain object or function is simpler and clearer.

### Decide: commit to tagless-final, or simplify

If you want to keep the tagless-final shape, consider:

1. Writing the parser program against the algebra, not its implementation.
2. Using `cats.MonadError` or a similar abstraction instead of a hand-rolled `Monadish` (or at least deriving `flatMap`/`map` syntax from it).
3. Providing at least two interpreters so the abstraction earns its keep — e.g. `Either` for production and a `State`-based or logging interpreter for tests.

If this project is primarily about learning cron parsing, it is completely fine to drop the tagless-final layer and just expose:

```scala
object CronParser {
  def parse(input: String): Either[ParserError, Cron] = ...
}
```

You can always reintroduce the abstraction later when you have a real need for it.

## Imperative patterns that fight Scala

You said imperative code fits your brain better, which is fair, but several of the imperative choices here make the code harder to reason about and more error-prone than necessary.

### Early `return`

There are early `return`s in `Lexer.lex`, `Lexer.handleDigit`, `Parser.parse`, and `Validator.validate`. In Scala, `return` is not a neutral control-flow tool: it desugars to throwing `NonLocalReturnControl`, which can behave unexpectedly with lambdas and higher-order functions. Idiomatic Scala expresses early exit through the data type, e.g.:

```scala
for {
  tokens <- lexer.lex(input)
  cron   <- validator.validate(tokens)
} yield cron
```

or, with `Either` directly:

```scala
lexer.lex(input).flatMap(validator.validate)
```

### Mutable lexer state

`Lexer` carries mutable `var _input`, `readPos`, `currPos`, and a mutable `ListBuffer`. That makes the lexer hard to reuse and hard to test. It also led to a test for resetting input, which is a smell: a parser should ideally be a pure function from `String` to `Either[Error, Tokens]`.

A more Scala-idiomatic lexer is either:

* A recursive function over `String` (or `List[Char]`), or
* Parser combinators (e.g. `cats-parse`), which let you write the grammar declaratively.

For learning, the recursive-function approach is a good middle ground:

```scala
def lex(input: String): Either[ParserError, List[AbstractCronFragment]] = {
  def loop(remaining: String, acc: List[AbstractCronFragment]): Either[ParserError, List[AbstractCronFragment]] =
    if remaining.isEmpty then Right(acc.reverse)
    else nextFragment(remaining).flatMap { case (frag, rest) => loop(rest, frag :: acc) }

  loop(input.trim, Nil)
}
```

### `ListBuffer` leaks into the domain model

```scala
type AbstractCronFragmentList = ListBuffer[AbstractCronFragment]
```

Prefer immutable `List` unless you can measure a performance reason for mutability. `ListBuffer` as a public alias makes the API harder to use and couples consumers to mutability. Internally it is fine for local performance, but the parser/validator boundary should speak `List`.

### `Cron.fromCronFragments` indexes into a buffer

```scala
def fromCronFragments(l: ListBuffer[CronFragment]): Cron =
  Cron(l(0), l(1), l(2), l(3), l(4))
```

This is unsafe (throws on wrong length) and brittle. Since you already zip fragments with `cronIntervalOrder` in the validator, consider constructing `Cron` explicitly there, or using a typed helper that pattern-matches on exactly five elements:

```scala
fragments match {
  case m :: h :: dom :: mo :: w :: Nil => Cron(m, h, dom, mo, w)
  case _                               => Left(CronInvalid(...))
}
```

### `boundary`/`break` in the validator

```scala
val items: Either[ParserError, ListBuffer[CronFragment]] = boundary {
  ...
  case _ => break(Left(OutOfIntervalBounds(ci, acf.raw)))
}
```

`boundary`/`break` is a valid Scala 3 feature, but using it to escape a `map` is a sign that you want `traverse`:

```scala
input.zip(cronIntervalOrder).traverse { (acf, ci) =>
  validateFragment(acf, ci).map(CronFragment(ci, _))
}
```

`traverse` turns `List[(A, B)]` into `F[List[C]]` while short-circuiting on the first error automatically. That is exactly the behaviour your `boundary`/`break` is implementing by hand.

## Smaller idiomatic points

### Redundant `val` on case-class parameters

```scala
case class Single(val raw: String, val value: Int) extends AbstractCronFragment
```

In Scala 3, case-class constructor parameters are already `val` (and public) by default. Write `case class Single(raw: String, value: Int)`.

### Don't put user-facing messages in `toString`

```scala
case class InvalidInput(raw: String, position: Int) extends ParserError {
  override def toString(): String = s"Invalid input at position $position. Near $raw"
}
```

`toString` is for debugging; user messages should be explicit, e.g.:

```scala
sealed trait ParserError {
  def message: String
}

case class InvalidInput(raw: String, position: Int) extends ParserError {
  def message: String = s"Invalid input at position $position. Near $raw"
}
```

This also makes your tests less brittle: right now several lexer tests fail because they assert on `toString` output that no longer matches the implementation (`at 1` vs `at position 1`).

### `EOC = 'E'` is a risky sentinel

Using `'E'` as an end-of-input marker assumes it can never appear in valid input. For cron that happens to be true, but it is clearer to model the current character as `Option[Char]`:

```scala
def peek(input: String, pos: Int): Option[Char] =
  if pos < input.length then Some(input.charAt(pos)) else None
```

### `readNumber` can throw

```scala
def readNumber(): Int = {
  val tempReadPos = readPos
  while (getReadToken().isDigit) incrementReadPos()
  _input.slice(tempReadPos, readPos).toInt
}
```

If the slice is empty, `.toInt` throws `NumberFormatException`. The surrounding code tries to guard against this, but the lexer should return `Either` instead of throwing.

### List parsing only supports exactly two items

`1,2,3` will not parse as a three-element list because `handleDigit` only reads one follow-up number after the comma. Standard cron lists are comma-separated with an arbitrary number of items.

### Avoid `getOrElse(())` on `Either`

```scala
val res = l.lex().getOrElse(())
assertEquals(expectedOut, res)
```

`()` has type `Unit`, so this compiles only because `expectedOut` is a `ListBuffer` and the equality check still works at runtime, but it is very fragile. Prefer:

```scala
val res = l.lex().getOrElse(fail("expected success"))
```

or pattern-match on `Right`.

## Suggested next steps

1. **Fix the failing tests** by aligning expected strings with actual `toString` output, or (better) stop asserting on `toString`.
2. **Pick one design direction:**
   * *Simple:* make `CronParser.parse(input: String): Either[ParserError, Cron]` a pure function and drop the unused algebra/Monadish scaffolding.
   * *Tagless-final:* write the parser program abstractly, use the `Monadish` constraint, and provide at least two interpreters.
3. **Replace mutable lexer state** with a recursive function or parser combinators.
4. **Replace `ListBuffer` in the domain API** with `List`.
5. **Replace `boundary`/`break` and early `return`s** with `flatMap`/`traverse`.
6. **Add support for lists with more than two elements**.

Overall, the domain model and test coverage are a strong base. The main thing holding the code back is that the functional/algebraic scaffolding is present but not connected to the imperative implementation underneath. Closing that gap — either by committing to the abstraction or removing it — will make the codebase feel much more idiomatic.
