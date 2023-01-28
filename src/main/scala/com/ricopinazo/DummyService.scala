package com.ricopinazo

import cats.Functor
import cats.effect.Sync
import cats.syntax.all._
import com.google.protobuf.empty.Empty
import com.ricopinazo.dummy.Dummy
import grpc.health.v1.health.HealthCheckResponse.ServingStatus
import higherkindness.mu.rpc.healthcheck.{HealthService, ServiceStatus}

class DummyService[F[_]: Functor](healthService: HealthService[F]) extends Dummy[F] {

  override def startServing(message: Empty): F[Empty] = {
    val status = ServiceStatus(DummyService.name, ServingStatus.SERVING)
    healthService.setStatus(status) as Empty()
  }

  override def stopServing(message: Empty): F[Empty] = {
    val status = ServiceStatus(DummyService.name, ServingStatus.NOT_SERVING)
    healthService.setStatus(status) as Empty()
  }
}

object DummyService {
  val name = "dummy"

  def build[F[_]: Sync](healthService: HealthService[F]): F[Dummy[F]] =
    Sync[F].pure(new DummyService[F](healthService))
}
