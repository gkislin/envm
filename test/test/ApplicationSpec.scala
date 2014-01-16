package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  
  "Application" should {
    
    "send 404 on a bad request" in {
      running(FakeApplication()) {
        route(FakeRequest(GET, "/boum")) must beNone        
      }
    }
    
    "render an empty form on index" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get
        
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
      }
    }
      "send BadRequest on form error" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/hello?name=Bob&repeat=xx")).get
        status(home) must equalTo(BAD_REQUEST)
        contentType(home) must beSome.which(_ == "text/html")
      }
    }
    "say hello" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/hello?name=Bob&repeat=10")).get
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
      }
    }
    "find pattern" in {
      val body = """
          <li><a href="bdg_payment--2.02.bar">bdg_payment--2.02.bar</a></li>
          <li><a href="ecm_acts--2.7.bar">ecm_acts--2.7.bar</a></li>
          <li><a href="ecm_agreements_close--1.2.bar">ecm_agreements_close--1.2.bar</a></li>
          <li><a href="ecm_agreements_general--2.3.bar">ecm_agreements_general--2.3.bar</a></li>
          <li><a href="ecm_authorities--2.3.bar">ecm_authorities--2.3.bar</a></li>
        """.stripMargin
      val name = "ecm_agreements_general"
      val pattern = s"$name--.+?\\.bar".r
      println(pattern findFirstIn body)
      pattern findFirstIn body must beSome[String]
    }
  } 
}
