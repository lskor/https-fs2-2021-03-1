package me.chuwy.otusfp

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  def run: IO[Unit] = {

    Homework.server.use{ _ =>
      IO.never
    }
  }
}
