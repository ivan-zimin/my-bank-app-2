package misis.kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import misis.WithKafka
import io.circe.generic.auto._
import misis.model.{TransferStart, TransferStarted}
import misis.repository.AccountRepository

import scala.concurrent.{ExecutionContext, Future}

class TransferStreams(repository: AccountRepository)(implicit val system: ActorSystem, executionContext: ExecutionContext)
  extends WithKafka {
  def group = s"account-${repository.startAccountId}"

  kafkaSource[TransferStart]
    .filter(command => repository.contains(command.from))
    .map { command =>
      repository.transferWithdraw(command)
    }
    .to(kafkaSink)
    .run()

  kafkaSource[TransferStarted]
    .filter(event => repository.contains(event.to))
    .map { event =>
      repository.transferAccrue(event)
    }
    .to(kafkaSink)
    .run()
}
