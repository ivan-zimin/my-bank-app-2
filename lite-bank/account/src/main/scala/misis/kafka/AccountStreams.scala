package misis.kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import io.circe.generic.auto._
import misis.WithKafka
import misis.model._
import misis.repository.AccountRepository

import scala.concurrent.{ExecutionContext, Future}

class AccountStreams(repository: AccountRepository)(implicit val system: ActorSystem, executionContext: ExecutionContext)
  extends WithKafka {

  def group = s"account-${repository.startAccountId}"

  kafkaSource[AccountUpdate]
    .filter(command => repository.contains(command.accountId))
    .map { command =>
      repository.updateAccount(command.accountId, command.value)
    }
    .to(kafkaSink)
    .run()

  kafkaSource[AccountCreate]
    .filter(command =>
      command.accountId >= repository.startAccountId &&
        command.accountId < repository.endAccountId)
    .map { command =>
      repository.createAccount(command.accountId)
    }
    .to(kafkaSink)
    .run()

  kafkaSource[AccountUpdated]
    .filter(event => repository.contains(event.accountId))
    .map { event =>
      repository.printUpdateResult(event)
      event
    }
    .to(Sink.ignore)
    .run()
}
