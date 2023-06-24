package misis.repository

import misis.model._


class AccountRepository(val startAccountId: Int, val endAccountId: Int) {

    private var accounts: List[Account] = List()

    def contains(accountId: Int): Boolean =
        if (accounts.indexWhere(_.id == accountId) == -1) false else true

    private def getBalance(accountId: Int): Int = {
        val idx = accounts.indexWhere(_.id == accountId)
        accounts(idx).amount
    }

    def transferWithdraw(command: TransferStart): TransferStarted = {
        val withdraw: AccountUpdated = updateAccount(command.from, -command.amount)
        printUpdateResult(withdraw)
        TransferStarted(command.to, command.amount, withdraw.success)
    }

    def transferAccrue(event: TransferStarted): Unit = {
        if(event.isSuccess){
            val accrue: AccountUpdated = updateAccount(event.to, event.amount)
            printUpdateResult(accrue)
        }
    }

    def printUpdateResult(res: AccountUpdated) =
        println(s"Account ${res.accountId} updated successful ${res.success}" +
            s" with value ${res.value} " +
            s"Balance: ${getBalance(res.accountId)}")

    def updateAccount(accountId: Int, value: Int): AccountUpdated = {
        val idx = accounts.indexWhere(_.id == accountId)
        val accountBefore = accounts(idx)
        if (accountBefore.amount + value >= 0) {
            val resAccount = accountBefore.update(value)
            accounts = accounts.map(account => if (account.id == resAccount.id) resAccount else account)
            AccountUpdated(accountId = accountId, value = value, success = true)
        } else {
            AccountUpdated(accountId = accountId, value = value, success = false)
        }
    }

    def createAccount(accountId: Int): AccountCreated =
        if (accounts.indexWhere(_.id == accountId) == -1) {
            val account = Account(accountId)
            accounts = accounts :+ account
            println(s"Account with id:${accountId} created")
            AccountCreated(accountId = accountId, success = true)
        } else {
            println(s"Account with id:${accountId} exists")
            AccountCreated(accountId = accountId, success = false)
        }

  def getAccInfo(accountId: Int): String = {
      if (contains(accountId)) {
          val idx = accounts.indexWhere(_.id == accountId)
          s"amount: ${accounts(idx).amount}"
      }
      else {
          ""
      }
  }
}
