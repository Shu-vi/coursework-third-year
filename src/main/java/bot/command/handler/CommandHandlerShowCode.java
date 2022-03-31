package bot.command.handler;

import bot.TelegramBot;
import database.DatabaseControl;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CommandHandlerShowCode {
    private TelegramBot telegramBot;
    private DatabaseControl databaseControl;

    public CommandHandlerShowCode(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
        this.databaseControl = DatabaseControl.getObjectDatabaseControl();
    }

    @SneakyThrows
    public void commandShowCode(Update update){
        Long userId = update.getMessage().getChatId();
        String text = "Ваш код: " + databaseControl.getUserCodeByUserId(userId);
        this.telegramBot.execute(SendMessage.builder().text(text).chatId(userId.toString()).build());
    }
}
