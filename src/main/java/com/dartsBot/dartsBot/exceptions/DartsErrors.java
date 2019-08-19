package com.dartsBot.dartsBot.exceptions;

import lombok.Data;

@Data
public class DartsErrors {
    public final static String WRONG_PLAYERS_COUNT = "Гораздо интересней играть в дартс вдвоем! Введите два имя через пробел";
    public final static String WRONG_COMMAND = "Комманда не распознана!";
    public final static String GAME_NOT_CREATE = "Вам неоткуда выходить. Вы не создавали игру";
    public final static String WRONG_NAME_SYMBOLS = "Наши имена пишутся без спецсимволов или цифр." +
            " Если вы пытались ввести счет то пишите букву и 3 цифры !через пробел!)";
    public final static String SAME_PREF = "Непонимаю кому записать очки. У них имена одинаково начинаются";
}
