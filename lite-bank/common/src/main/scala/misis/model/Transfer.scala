package misis.model

import java.util.UUID

case class TransferStart(from: Int, to: Int, amount: Int) extends Command


case class TransferStarted(to: Int, amount: Int, isSuccess: Boolean) extends Event

