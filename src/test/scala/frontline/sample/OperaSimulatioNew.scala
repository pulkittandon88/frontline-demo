
package frontline.sample

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random

class OperaSimulationNew  extends Simulation {

        val httpProtocol = http
                .baseUrl("https://opera-test-public.staging-test.illusionsdraw.com/")
                .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:88.0) Gecko/20100101 Firefo/88.0")
                .disableCaching
                .disableFollowRedirect
        val feeder = Iterator.continually(Map("testheader" -> (Random.alphanumeric.take(20).mkString + "@foo.com")))


    def getBasepage() = {
      repeat(1) {
        exec(http("Get Base Page")
          .get("/")
          .headers(Map("testheader" -> "${testheader}", "Connection" -> "close"))
          .check(status.is(200))
                )
      }
    }


    //val scn = scenario("BookinfoSimulation")
    //  .exec(getProductpage())
    //  //.pause(5)
    //  .exec(passDetails())
        //  //.pause(5)
    //  .exec(failDetail())


        val scn = scenario("Fixed Duration Load Simulation")
          .feed(feeder)
          .exec(getBasepage())

        setUp(
                  scn.inject(
                  rampUsers(5000) during (10 seconds),
                  constantUsersPerSec(5000) during (110 seconds)
                  ).protocols(httpProtocol)
        )

}
