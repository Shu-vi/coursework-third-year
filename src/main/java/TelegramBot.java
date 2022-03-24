import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Optional;

public class TelegramBot extends TelegramLongPollingBot {
    private static final String TOKEN = GetProperties.token();
    private static final String USERNAMEBOT = GetProperties.botUserName();



    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public String getBotUsername() {
        return USERNAMEBOT;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String type;
        type = getTypeUpdate(update);
        doAction(type, update);
        handleMessage(update.getMessage());
    }
    private String getTypeUpdate(Update update){
        String type = null;
        //Определяем тип апдейта
        return "message";
    }

    private void doAction(String type, Update update){
        //В зависимости от типа делаем что-то с абдейтом
    }
    @SneakyThrows
    private void handleMessage(Message message){//Распознаём команды
        if (message.hasText() && message.hasEntities()){
            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e->"bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()){
                String command = message.getText();
                switch (command){
                    case "/help":
                        execute(
                                SendMessage.builder()
                                        .text("Типа помощь")
                                        .chatId(message.getChatId().toString())
                                        .build()
                        );
                        return;
                    default:
                        execute(
                                SendMessage.builder()
                                        .text("Команда не распознана")
                                        .chatId(message.getChatId().toString())
                                        .build()
                        );
                }
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        TelegramBot telegramBot = new TelegramBot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBot);
    }
}
