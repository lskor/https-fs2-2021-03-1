package me.chuwy.otusfp

import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.global

object Server {

	val route: HttpRoutes[IO] = HttpRoutes.of {

			case GET -> Root / "counter" =>
				Ok("""{"counter": "1"}""")

			case GET -> Root / "slow" / chunk / total / time =>
				Ok(s"$chunk, $total, $time")
		}

	val server =
		BlazeServerBuilder[IO](global)
			.bindHttp(port = 8080, host = "localhost")
			.withHttpApp(route.orNotFound)
			.resource
}