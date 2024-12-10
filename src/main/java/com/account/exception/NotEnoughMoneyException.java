package com.account.exception;

public class NotEnoughMoneyException extends RuntimeException {

    public NotEnoughMoneyException(Long accountId) {
        super("There are insufficient funds in the account " + accountId);
    }
}
