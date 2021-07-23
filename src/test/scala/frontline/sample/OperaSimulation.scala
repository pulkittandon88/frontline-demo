
package frontline.sample

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random

class OperaSimulation  extends Simulation {

        val httpProtocol = http
                .baseUrl("https://opera-test-public.staging.volterra.us")
                .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:88.0) Gecko/20100101 Firefo/88.0")
                .disableCaching
        val feeder = Iterator.continually(Map("testheader" -> (Random.alphanumeric.take(20).mkString + "@foo.com")))


    def getBasepage() = {
      repeat(1) {
        exec(http("Get Base Page")
          .get("/")
          .headers(Map("testheader" -> "${testheader}"))
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
          .forever() {
               exec(getBasepage())
          }

        setUp(
                  scn.inject(
                  atOnceUsers(1400)
                  ).protocols(httpProtocol)
        ).throttle(
        reachRps(4000).in(1.minute),
        holdFor(1.minute),
    )

}
