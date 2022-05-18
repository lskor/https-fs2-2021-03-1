package me.chuwy.otusfp

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import fs2.text.utf8Decode
import me.chuwy.otusfp.Server.{Env, mkEnv}
import org.http4s.implicits._
import org.http4s.{Request, Uri}
import org.specs2.mutable.Specification


class ServerSpec extends Specification with CatsEffect {

	private val counterUri = uri"http://localhost:8080/counter"
	private val slowUri = uri"http://localhost:8080/slow/2/6/1"
	private val badSlowUri = uri"http://localhost:8080/slow/5/-16/5"
	private val notMatchUri = uri"http://localhost:8080/slow/5/"

	"MyServerSpec" should {

		"counter 0" in {

			mkEnv.flatMap(env => run(counterUri, env))
				.map(_ must beEqualTo("""{"counter":0}"""))
		}

		"counter 2" in {

			val actual = for {
				env <- mkEnv
				_ <- run(counterUri, env)
				_ <- run(counterUri, env)
				result <- run(counterUri, env)
			} yield result

			actual.map(_ must beEqualTo("""{"counter":2}"""))
		}

		"slow numbers" in mkEnv.flatMap(run(slowUri, _).map(_ must beEqualTo("\"01\"\"23\"\"45\"")))

		"bad parameters" in mkEnv.flatMap(run(badSlowUri, _).map(_ must beEqualTo("\"Parameters must be positive\"")))

		"no matches" in mkEnv.flatMap(run(notMatchUri, _).map(_ must beEqualTo("No matches")))
	}

	def run(uri:Uri, env: Env): IO[String] = {
		val request = Request[IO]().withUri(uri)

		Server.route(env).run(request).value.flatMap {
			case Some(res) => res.body.through(utf8Decode).compile.string
			case None => IO.pure("No matches")
		}
	}
}