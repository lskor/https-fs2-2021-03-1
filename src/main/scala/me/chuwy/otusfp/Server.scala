package me.chuwy.otusfp

import cats.effect._
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{EntityEncoder, HttpRoutes}

import scala.concurrent.ExecutionContext.global

object Server {

	case class CounterValue(counter: Int)

	implicit val counterEncoder: Encoder[CounterValue] =
		deriveEncoder[CounterValue]

	implicit val userEntityEncoder: EntityEncoder[IO, CounterValue] =
		jsonEncoderOf[IO, CounterValue]

	case class Env(ref: Ref[IO, Int])
	val mkEnv: IO[Env] = Ref.of[IO, Int](0).map(Env.apply)

	def route(env: Env): HttpRoutes[IO] = HttpRoutes.of {

			case GET -> Root / "counter" =>
				env.ref.getAndUpdate(_ + 1)
					.flatMap(current => Ok(CounterValue(current)))

			case GET -> Root / "slow" / chunk / total / time =>
				Ok(s"$chunk, $total, $time")
		}

	def server(env: Env) =
		BlazeServerBuilder[IO](global)
			.bindHttp(port = 8080, host = "localhost")
			.withHttpApp(route(env).orNotFound)
			.resource
}