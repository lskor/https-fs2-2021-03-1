package me.chuwy.otusfp

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import fs2.text.utf8Decode
import org.http4s.implicits._
import org.http4s.{Request, Uri}
import org.specs2.mutable.Specification


class ServerSpec extends Specification with CatsEffect {

	"MyServerSpec" should {
		"counter 1" in {
			run(uri"http://localhost:8080/counter").map(_ must beEqualTo("""{"counter": "1"}"""))
		}

		"slow numbers" in {
			run(uri"http://localhost:8080/slow/10/1024/5").map(_ must beEqualTo("10, 1024, 5"))
		}

		"no matches" in {
			run(uri"http://localhost:8080/slow/10").map(_ must beEqualTo("No matches"))
		}

		"no matches" in {
			run(uri"http://localhost:8080/slow/10").map(_ must beEqualTo("No matches"))
		}
	}

	def run(uri: Uri): IO[String] = {
		val request = Request[IO]().withUri(uri)

		Server.route.run(request).value.flatMap {
			case Some(res) => res.body.through(utf8Decode).compile.string
			case None => IO.pure("No matches")
		}
	}
}