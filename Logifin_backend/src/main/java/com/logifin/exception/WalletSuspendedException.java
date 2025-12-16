package com.logifin.exception;

public class WalletSuspendedException extends RuntimeException {
    public WalletSuspendedException(String message) {
        super(message);
    }
}
