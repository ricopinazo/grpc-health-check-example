package com.ricopinazo

import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
import com.ricopinazo.dummy.Dummy
import grpc.health.v1.health.Health
import higherkindness.mu.rpc.healthcheck.HealthService
import higherkindness.mu.rpc.server.{AddService, GrpcServer}
import io.grpc.ServerServiceDefinition

object Server extends IOApp {

  val port = 8000

  private def services[F[_]: Async] : Resource[F, List[ServerServiceDefinition]] =
    for {
      health    <- Resource.eval(HealthService.build[F])
      healthDef <- Health.bindService[F](Async[F], health)
      dummy     <- Resource.eval(DummyService.build[F](health))
      dummyDef  <- Dummy.bindService[F](Async[F], dummy)
    } yield List(healthDef, dummyDef)

  def runServices[F[_]: Async]: F[ExitCode] = services[F].use { services =>
    for {
      config <- Async[F].pure(services.map(service => AddService(service)))
      server <- GrpcServer.default[F](port, config)
      _      <- GrpcServer.server[F](server)
    } yield ExitCode.Success
  }

  override def run(args: List[String]): IO[ExitCode] = runServices[IO]
}
