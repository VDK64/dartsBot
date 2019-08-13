package com.dartsBot.dartsBot.mainBot;

import com.dartsBot.dartsBot.entities.Match;
import com.dartsBot.dartsBot.exceptions.DartsErrors;
import com.dartsBot.dartsBot.exceptions.DartsExceptions;
import com.dartsBot.dartsBot.repository.MatchRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ThreadBot extends Thread {
    private MatchRepository matchRepo;
    private MainDarts mainDarts;
    private Update update;
    private Message message;
    private static final String regexNum = "\\d+";
    private static final String regexLetters = "[\\sa-zA-Zа-яА-ЯёЁ]+";

    public ThreadBot(Update update, MatchRepository matchRepo, MainDarts mainDarts) {
        this.update = update;
        this.message = update.getMessage();
        this.matchRepo = matchRepo;
        this.mainDarts = mainDarts;
    }

    @Override
    public void run() {
        SendMessage response = new SendMessage();
        try {
            switch (message.getText()) {
                case "/start":
                    response = formMessage(message, "Добро пожаловать в DartsBot! Юзни /info и все поймешь!", response);
                    response.setReplyMarkup(keyBoard(Collections.singletonList("/NewGame")));
                    break;
                case "/NewGame":
                    response = formMessage(message, "Введите имена игроков через пробел", response);
                    break;
                case "/StopGame":
                    response.setReplyMarkup(keyBoard(Collections.singletonList("/NewGame")));
                    response = stopGame(message, response);
                    break;
                case "/info":
                    response = formMessage(message, formInfoText(), response);
                    break;
                default:
                    response = gameLogic(message);
            }
        } catch (DartsExceptions e) {
            response = formMessage(message, e.getMessage(), response);
        }
        mainDarts.exeResponse(response);
    }

    private SendMessage playersCreateOrUpdate() {
        SendMessage response = new SendMessage();
        String request = message.getText();
        if (!matchRepo.findById(message.getChatId()).isPresent()) {
            if (request.matches(regexLetters)) {
                if (request.split(" ").length == 2) {
                    response = createNewGame(update);
                    response.setReplyMarkup(keyBoard(Collections.singletonList("/StopGame")));
                } else throw new DartsExceptions(DartsErrors.WRONG_PLAYERS_COUNT);
            } else throw new DartsExceptions(DartsErrors.WRONG_NAME_SYMBOLS);
        } else {
            if (request.matches(regexLetters)) {
                if (request.split(" ").length == 2) {
                    Match match = matchRepo.findById(message.getChatId()).get();
                    match.setPlayer1Name(request.split(" ")[0].toUpperCase());
                    match.setPlayer2Name(request.split(" ")[1].toUpperCase());
                    matchRepo.save(match);
                    response = formMessage(message, tableInfo(match), response);
                } else throw new DartsExceptions(DartsErrors.WRONG_PLAYERS_COUNT);
            } else if (request.split(" ").length != 4){ throw new DartsExceptions(DartsErrors.WRONG_NAME_SYMBOLS); }
        }
        return response;
    }

    private SendMessage gameLogic(Message message) {
        SendMessage response = playersCreateOrUpdate();
        if (response.getText() == null && response.getChatId() == null) {
            String[] arrayReq = message.getText().split(" ");
            if (arrayReq.length == 4) {
                if (arrayReq[0].matches(regexLetters) && arrayReq[1].matches(regexNum) && arrayReq[2].matches(regexNum)
                        && arrayReq[3].matches(regexNum)) {
                    Match match = matchRepo.findById(message.getChatId()).get();
                    String pref = arrayReq[0].toUpperCase();
                    int sum = Integer.parseInt(arrayReq[1]) + Integer.parseInt(arrayReq[2]) + Integer.parseInt(arrayReq[3]);
                    match = pointsAndScoreLogic(match, pref, sum);
                    matchRepo.save(match);
                    response = formMessage(message, tableInfo(match), response);
                } else throw new DartsExceptions(DartsErrors.WRONG_COMMAND);
            } else throw new DartsExceptions(DartsErrors.WRONG_COMMAND);
        }
        return response;
    }

    private Match pointsAndScoreLogic(Match match, String pref, int sum) {
        if (match.getPlayer1Name().startsWith(pref) && match.getPlayer2Name().startsWith(pref))
            throw new DartsExceptions(DartsErrors.SAME_PREF);
        if (match.getPlayer1Name().startsWith(pref)) {
            int buf = match.getPlayer1points();
            match.setPlayer1points(match.getPlayer1points() - sum);
            if (match.getPlayer1points() == 0) {
                match.setPlayer1Score(match.getPlayer1Score() + 1);
                match.setPlayer1points(301);
            }
            if (match.getPlayer1points() < 0)
                match.setPlayer1points(buf);
        }
        if (match.getPlayer2Name().startsWith(pref)) {
            int buf = match.getPlayer2points();
            match.setPlayer2points(match.getPlayer2points() - sum);
            if (match.getPlayer2points() == 0) {
                match.setPlayer2Score(match.getPlayer2Score() + 1);
                match.setPlayer2points(301);
            }
            if (match.getPlayer2points() < 0)
                match.setPlayer2points(buf);
        }
        return match;
    }

    private SendMessage stopGame(Message message, SendMessage response) {
        Match match = matchRepo.findById(message.getChatId()).orElseThrow(() -> new DartsExceptions(
                DartsErrors.GAME_NOT_CREATE));
        matchRepo.delete(match);
        return formMessage(message, "Вы вышли из игры", response);
    }

    private ReplyKeyboardMarkup keyBoard(List<String> buttons) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keys = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        for (int i = 0; i < buttons.size(); i++) {
            keyboardRow.add(i, buttons.get(i));
        }
        keyboardRow.add("/info");
        keys.add(keyboardRow);
        keyboard.setKeyboard(keys);
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    private SendMessage formMessage(Message message, String text, SendMessage response) {
        response.setChatId(message.getChatId());
        response.setText(text);
        return response;
    }

    private String tableInfo(Match match) {
        return match.getPlayer1Name() + "  " + match.getPlayer1points() + '\n' + match.getPlayer2Name() + "  "
                + match.getPlayer2points() + '\n' + "Total Score: " + match.getPlayer1Score() + " | " + match.getPlayer2Score();
    }

    private SendMessage createNewGame(Update update) throws DartsExceptions {
        String[] players = update.getMessage().getText().toUpperCase().split(" ");
        if (players.length > 2) {
            throw new DartsExceptions(DartsErrors.WRONG_PLAYERS_COUNT);
        }
        Match match = new Match(update.getMessage().getChatId(), players[0], players[1], 301, 301,
                0, 0);
        matchRepo.save(match);
        String response = players[0] + "  " + match.getPlayer1points() + '\n' + players[1] + "  " + match.getPlayer2points() + '\n' +
                "Total Score: 0 | 0" + '\n' + "В дальнейшем, вводите первую букву имени(первые буквы, если имена" +
                " начинаются одинаково), а затем 3 результата бросков через пробел. Если имена поменять захотите, введите" +
                " их через пробел";
        return formMessage(update.getMessage(), response, new SendMessage());
    }

    private String formInfoText() {
        return "Этот бот разработан для подсчета очков двух игроков, которые увлеченно играют в дартс и не хотят ничего " +
                "считать с калькулятором. Пользуйтесь кнопками телеграмм - клавиатуры для навигации по режимам. Вы можете" +
                " в любой момент написать подряд два имя через пробел и ваши имена поменяются. Для ввода очков пишите " +
                "первую букву имени (если буквы похоже, то несколько первых), а потом три цифры через пробел. Пример: " +
                "М 50 30 10(Михаил). Разобраться с ботом легко, считать в уме сложно и накладно, так что пользуйтесь, наслаждайтесь))";
    }
}
