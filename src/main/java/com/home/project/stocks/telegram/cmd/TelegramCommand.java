package com.home.project.stocks.telegram.cmd;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author rlagay
 */
public interface TelegramCommand {
    String execute(Update update);
}
