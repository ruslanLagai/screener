package com.home.project.stocks.telegram;

import com.home.project.stocks.model.entity.TelegramChatEntity;
import com.home.project.stocks.model.telegram.ChatStatus;
import com.home.project.stocks.repository.CandleRepository;
import com.home.project.stocks.repository.ChatRepository;
import com.home.project.stocks.telegram.cmd.TelegramCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot implements TelegramNotificationBot {

    private final CandleRepository candleRepository;
    private final ChatRepository chatRepository;
    private final TelegramCommand subscribeCommand;
    private final TelegramCommand stopCommand;
    private final Map<String, Function<Update, String>> telegramCommands = new HashMap<>();
    private static final Set<String> COMMANDS = Set.of("/start", "/stop");

    @Value("${telegram.bot.username}")
    private String username;

    @Value("${telegram.bot.token}")
    private String token;

    @Getter
    @Setter
    private double userSpecificThreshold = 1.0;

    public TelegramBot(CandleRepository candleRepository,
                       ChatRepository chatRepository,
                       TelegramCommand subscribeCommand,
                       TelegramCommand stopCommand) {
        this.candleRepository = candleRepository;
        this.chatRepository = chatRepository;
        this.subscribeCommand = subscribeCommand;
        this.stopCommand = stopCommand;
    }

    @PostConstruct
    public void initChats() {
        var activeChats = chatRepository.findByStatus(ChatStatus.ACTIVE).stream()
                .map(TelegramChatEntity::getId)
                .collect(Collectors.toSet());
        telegramCommands.put("/start", subscribeCommand::execute);
        telegramCommands.put("/stop", stopCommand::execute);
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void sendNotification() {
        final var dayOfWeek = LocalDateTime.now().getDayOfWeek();
        var stocksWithPattern = candleRepository.findByTimeAfter(dayOfWeek.equals(DayOfWeek.MONDAY)
                ? LocalDateTime.now().minusDays(3) : LocalDateTime.now().minusDays(1));

        log.info("Found {} stocks with pattern", stocksWithPattern.size());
        if (CollectionUtils.isEmpty(stocksWithPattern)) {
            return;
        }

        chatRepository.findByStatus(ChatStatus.ACTIVE).stream()
                .map(TelegramChatEntity::getId)
                .collect(Collectors.toSet())
                .forEach(chatId -> {
                    var message = new StringBuilder().append("\uD83D\uDFE2 Stocks from S&P with patterns: \n\n");
                    stocksWithPattern
                            .forEach(candle -> message.append("\ud83d\ude80 ").append(candle.toString()).append("\n\n"));
                    var request = new SendMessage(chatId.toString(), message.toString());
                    request.disableWebPagePreview();
                    request.enableMarkdown(true);
                    try {
                        execute(request);
                    } catch (TelegramApiException e) {
                        log.error("Failed to notify user, chatId {}", chatId, e);
                    }
                });

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            var chatId = message.getChatId();
            var reply = telegramCommands.containsKey(message.getText())
                    ? telegramCommands.get(message.getText()).apply(update)
                    : "\u2139 No suitable command found. Available commands are: " + Arrays.toString(COMMANDS.toArray());


            var sm = new SendMessage();
            sm.setChatId(chatId.toString());
            sm.enableMarkdown(true);
            sm.setText(reply);

            try {
                execute(sm);
            } catch (TelegramApiException e) {
                log.error("Failed to send reply, {}", e.getMessage());
            }
        }
    }
}
