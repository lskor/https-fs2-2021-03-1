package me.chuwy.otusfp

import cats.effect._

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{EntityEncoder, HttpRoutes}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.DurationInt

object Server {

	case class CounterValue(counter: Int)

	implicit val counterEncoder: Encoder[CounterValue] =
		deriveEncoder[CounterValue]

	implicit val userEntityEncoder: EntityEncoder[IO, CounterValue] =
		jsonEncoderOf[IO, CounterValue]

	case class Env(ref: Ref[IO, Int])
	val mkEnv: IO[Env] = Ref.of[IO, Int](0).map(Env.apply)

	def slowStream(chunk: Int, total: Int, time: Int): fs2.Stream[IO, String] =
		fs2.Stream.emits(0 to 9)
			.repeat
			.take(total) // как-будто один символ равен 1 байту :)
			.chunkN(chunk)
			.map(_.toList.mkString) zipLeft fs2.Stream.awakeEvery[IO](time.second)

	def route(env: Env): HttpRoutes[IO] = HttpRoutes.of {

			case GET -> Root / "counter" =>
				env.ref.getAndUpdate(_ + 1)
					.flatMap(current => Ok(CounterValue(current)))

			case GET -> Root / "slow" / IntVar(chunk) / IntVar(total) / IntVar(time) =>
				if (chunk < 0 || total < 0 || time < 0 ) BadRequest("Parameters must be positive")
				else Ok(slowStream(chunk, total, time))
		}

	def server(env: Env) =
		BlazeServerBuilder[IO](global)
			.bindHttp(port = 8080, host = "localhost")
			.withHttpApp(route(env).orNotFound)
			.resource
}