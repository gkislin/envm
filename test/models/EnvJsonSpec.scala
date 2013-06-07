package models
import org.specs2.mutable._

/**
 * User: gkislin
 * Date: 04.06.13
 */
class EnvJsonSpec extends Specification {

  EnvJson.loadEnv should {
    "contain 11 characters" in {
      "Hello world" must have size (11)
    }
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }
}
