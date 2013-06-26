package models

import org.specs2.mutable._

/**
 * User: gkislin
 * Date: 04.06.13
 */
class EnvSpec extends Specification {
  {
    println("admin/gfhjkm".split('/') match {
      case Array() => ("", "")
      case Array(val1) => (val1, "")
      case Array(val1, val2) => (val1, val2)
    })

    println(getLoginPassw)

    def getLoginPassw: (String, String) = {
      "admin/gfhjkm".split('/') match {
        case Array() => ("", "")
        case Array(val1) => (val1, "")
        case Array(val1, val2) => (val1, val2)
      }
    }
  }
}
