package me.chuwy.otusfp

import cats.effect.{IO, IOApp}
import me.chuwy.otusfp.Server.mkEnv

object Main extends IOApp.Simple {

  def run: IO[Unit] = {

    mkEnv.flatMap { env =>
      Server.server(env).use { _ =>
        IO.never
      }
    }
  }
}
