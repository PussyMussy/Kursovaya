package org.example;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot {
    // Создайте своего бота, передав токен, полученный от @BotFather
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));
    private final String PROCESSING_LABEL = "Обработка...";
    private final static List<String> opponentWins = new ArrayList<String>() {{
        add("10");
        add("21");
        add("02");
    }};
    private final static Map<String, String> emojis = new HashMap<String, String>(){{
        put("0", "\uD83D\uDDFF");
        put("1", "✂");
        put("2", "\uD83D\uDCDC");
    }};

    public void serve() {
        // регестрируемся для получения обновлений
        bot.setUpdatesListener(updates -> {
            // ... обработка обновлений
            updates.forEach(this::process);
            // возвращает идентификатор последнего обработанного обновления или подтверждает их все
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();

        BaseRequest request = null;

        if (message != null && message.viaBot() != null && message.viaBot().username().equals("KrestikGame_bot")) {

            if (message.replyMarkup() == null) {
                return;
            }

            InlineKeyboardButton[][] buttons = message.replyMarkup().inlineKeyboard();

            if (buttons == null) {
                return;
            }

            InlineKeyboardButton button = buttons[0][0];
            String buttonLabel = button.text();
            if (!buttonLabel.equals(PROCESSING_LABEL)) {
                return;
            }

            Long chatId = message.chat().id();
            String senderName = message.from().firstName();
            Integer messageId = message.messageId();
            String senderChoose = button.callbackData();

            request = new EditMessageText(chatId, messageId, message.text())
                    .replyMarkup(
                            new InlineKeyboardMarkup(
                                    new InlineKeyboardButton("🗿")
                                            .callbackData(String.format("%d;;%s;;%s;;%s;;%d", chatId, senderName, senderChoose, "0", messageId)),
                                    new InlineKeyboardButton("✂️")
                                            .callbackData(String.format("%d;;%s;;%s;;%s;;%d", chatId, senderName, senderChoose, "1", messageId)),
                                    new InlineKeyboardButton("\uD83D\uDCDC")
                                            .callbackData(String.format("%d;;%s;;%s;;%s;;%d", chatId, senderName, senderChoose, "2", messageId))
                            )
                    );

        } else if (inlineQuery != null) {

            InlineQueryResultArticle stone = buildInlineButton("камень", "Камень \uD83D\uDDFF", "0");
            InlineQueryResultArticle scissors = buildInlineButton("ножницы", "Ножницы ✂️", "1");
            InlineQueryResultArticle paper = buildInlineButton("бумага", "Бумага \uD83D\uDCDC", "2");

            request = new AnswerInlineQuery(inlineQuery.id(), stone, scissors, paper).cacheTime(1);

        } else if (callbackQuery != null) {

            String[] data = callbackQuery.data().split(";;");
            Long chatId = Long.parseLong(data[0]);
            String senderName = data[1];
            String senderChoise = data[2];
            String opponentChoise = data[3];
            Integer oldMessageId = Integer.parseInt(data[4]);
            String opponentName = callbackQuery.from().firstName();

            if (senderChoise.equals(opponentChoise)) {
                request = new SendMessage(chatId, "Никто не победил!");
                DeleteMessage deleteMessage = new DeleteMessage(chatId, oldMessageId);
                bot.execute(deleteMessage);
            } else if (opponentWins.contains(senderChoise + opponentChoise)) {
                request = new SendMessage(chatId,
                        String.format(
                                "%s (%s) был побежден %s (%s)",
                                senderName, emojis.get(senderChoise), opponentName, emojis.get(opponentChoise)
                        )
                );
                DeleteMessage deleteMessage = new DeleteMessage(chatId, oldMessageId);
                bot.execute(deleteMessage);
            } else {
                request = new SendMessage(chatId,
                        String.format(
                                "%s (%s) был побежден %s (%s)",
                                opponentName, emojis.get(opponentChoise), senderName, emojis.get(senderChoise)
                        )
                );
                DeleteMessage deleteMessage = new DeleteMessage(chatId, oldMessageId);
                bot.execute(deleteMessage);
            }
        }

        if (request != null) {
            BaseResponse response = bot.execute(request);
        }
    }

    private InlineQueryResultArticle buildInlineButton(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(
                id, title, "Я готов к сражению!")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton(PROCESSING_LABEL).callbackData(callbackData)
                        )
                );
    }
}
