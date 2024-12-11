package com.account.exception;

public class NotEnoughMoneyException extends RuntimeException {

    public NotEnoughMoneyException(Long accountId) {
        super("На счете с ID " + accountId + " недостаточно средств");
    }
}
