package models

import org.specs2.mutable._

/**
 * User: gkislin
 * Date: 04.06.13
 */
class EnvSpec extends Specification {
  {
    "start with {" in {
      Env.jsonValue must
        startWith("{")
    }
  }
}
