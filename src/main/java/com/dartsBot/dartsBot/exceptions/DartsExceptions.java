package com.dartsBot.dartsBot.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DartsExceptions extends RuntimeException {
    public DartsExceptions(String message) {
        super(message);
    }
}
