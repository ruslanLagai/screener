package com.home.project.stocks.telegram.cmd;

import com.home.project.stocks.service.DbUpdateService;
import com.home.project.stocks.telegram.ChatType;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author rlagay
 */
@Component
public class SubscribeCommand implements TelegramCommand {

    private final DbUpdateService dbUpdateService;

    public SubscribeCommand(DbUpdateService dbUpdateService) {
        this.dbUpdateService = dbUpdateService;
    }

    @Override
    public String execute(Update update) {
        if (ChatType.parse(update.getMessage().getChat().getType()) != null
                && !update.getMessage().getFrom().getIsBot()) {
            dbUpdateService.activateTelegramChat(update);
            return "Notifications are enabled";
        }
        return "Failed to enable notifications";
    }
}
