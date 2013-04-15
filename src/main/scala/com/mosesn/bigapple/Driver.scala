import javax.net.ssl.SSLContext

import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}

import com.socrata.future.ExecutionContext.implicits._
import com.socrata.soda2.consumer.http.HttpConsumer
import com.socrata.soda2.values.SodaString
import com.socrata.http.NoAuth

object Driver {
  def main(args: Array[String]) {

    val clientConfig = new AsyncHttpClientConfig.Builder().
      setSSLContext(SSLContext.getDefault). // Without this, ALL SSL certificates are treated as valid
      build()
    val client = new AsyncHttpClient(clientConfig)
    try {
      val service = new HttpConsumer(client, "explore.data.gov", 443, NoAuth)

      // "select distinct(firstname) where lastname = 'clinton'" but
      // soda2 does not (yet) support "distinct".
      val future = service.query("644b-gaut", "namelast" -> "clinton").foldLeft(Set.empty[String]) { (firstNames, row) =>
        row("namefirst") match {
          case Some(SodaString(firstName)) => firstNames + firstName
          case _ => firstNames
        }
      }

      println("Waiting...")
      println(future())
      println("Done.")
    } finally {
      client.close()
    }

  }
}
