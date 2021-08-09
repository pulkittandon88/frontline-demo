
package simulations

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class BookinfoSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("http://bookinfo.automation.staging.volterra.us")
		.inferHtmlResources()
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.doNotTrackHeader("1")
		.disableCaching
		.header("Accept", "application/json")
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:88.0) Gecko/20100101 Firefox/88.0")

	val headers_0 = Map("Upgrade-Insecure-Requests" -> "1")

    def getProductpage() = {
      repeat(10) {
        exec(http("Get ProductPage")
          .get("/")
	      .headers(headers_0)
          .check(status.is(200))
		  //.check(jsonPath("$.html.head.title").is("Simple Bookstore App")))
		  .check(substring("Simple Bookstore App").find.exists))
      }
    }

    def passDetails() = {
      repeat(10) {
        exec(http("Pass details page")
          .get("/productpage?u=normal")
		  .headers(headers_0)
          .check(status.in(200 to 210)))
      }
    }

	def failDetail() = {
      repeat(10) {
        exec(http("Fail details Page")
          .get("/productpage?u=test")
          .headers(headers_0)
	      .check(status.is(403)))
      }
    }

    //val scn = scenario("BookinfoSimulation")
    //  .exec(getProductpage())
    //  //.pause(5)
    //  .exec(passDetails())
	//  //.pause(5)
    //  .exec(failDetail())

	val scn = scenario("Fixed Duration Load Simulation")
	  .forever() {
	       exec(getProductpage())
		  .exec(passDetails())
		  .exec(failDetail())
	  }

	setUp(
		scn.inject(
		  nothingFor(5 seconds),
		  atOnceUsers(10),
		  rampUsers(50) during (30 seconds),
		  nothingFor(10 seconds),
		  constantUsersPerSec(10) during (5 seconds),
		  nothingFor(10 seconds),
		  rampUsersPerSec(1) to (10) during (10 seconds)
		).protocols(httpProtocol)
	).maxDuration(2 minute)
     .assertions(global.responseTime.max.lt(20000), global.successfulRequests.percent.gt(80))

}
