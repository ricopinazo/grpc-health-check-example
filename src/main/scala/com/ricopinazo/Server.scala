package com.ricopinazo

import cats.effect.{Async, IO, Resource, ResourceApp}
import com.ricopinazo.dummy.Dummy
import grpc.health.v1.health.Health
import higherkindness.mu.rpc.healthcheck.HealthService
import higherkindness.mu.rpc.server.{AddService, GrpcServer}

object Server extends ResourceApp.Forever {

  val port = 8000

  def makeServer[F[_]: Async]: Resource[F, Unit] =
    for {
      health    <- Resource.eval(HealthService.build[F])
      healthDef <- Health.bindService[F](Async[F], health)
      dummy     <- Resource.eval(DummyService.build[F](health))
      dummyDef  <- Dummy.bindService[F](Async[F], dummy)
      services   = List(AddService(healthDef), AddService(dummyDef))
      server    <- Resource.eval(GrpcServer.default[F](port, services))
      _         <- GrpcServer.serverResource[F](server)
    } yield ()

  override def run(args: List[String]): Resource[IO, Unit] = makeServer
}
