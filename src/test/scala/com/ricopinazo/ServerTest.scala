package com.ricopinazo

import cats.effect.{IO, Resource}
import com.google.protobuf.empty.Empty
import com.ricopinazo.dummy.Dummy
import grpc.health.v1.health.{Health, HealthCheckRequest}
import higherkindness.mu.rpc.ChannelForAddress
import munit.CatsEffectSuite

import scala.language.postfixOps

class ServerTest extends CatsEffectSuite {
  test("Dummy service switches the health state when requested to start and stop serving") {
    val clients =
      for {
        _      <- Resource.make(Server.runServices[IO].start)(fiber => fiber.cancel)
        health <- Health.client[IO](ChannelForAddress("localhost", Server.port))
        dummy  <- Dummy.client[IO](ChannelForAddress("localhost", Server.port))
      } yield (health, dummy)

    clients.use {
      case (health, dummy) =>
        for {
          _        <- dummy.startServing(Empty())
          response <- health.Check(HealthCheckRequest(DummyService.name))
          _        <- IO.pure(assert(response.status.isServing))

          _        <- dummy.stopServing(Empty())
          response <- health.Check(HealthCheckRequest(DummyService.name))
          _        <- IO.pure(assert(response.status.isNotServing))
        } yield ()
    }
  }
}
