package bot;

import bot.command.handler.*;
import database.DatabaseControl;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import properties.GetProperties;
import java.util.ArrayList;
import java.util.List;
import static bot.Type.*;

public class TelegramBot extends TelegramLongPollingBot {
    private static final String TOKEN = GetProperties.token();
    private static final String USERNAMEBOT = GetProperties.botUserName();

    /**
     * Объект клавиатуры для команды /help
     */
    private InlineKeyboardMarkup inlineKeyboardMarkup;
    /**
     * Тип поступаемого Update.
     */
    private Type type;
    /**
     * Обработчики команд
     */
    private CommandHandlerStart commandHandlerStart;
    private CommandHandlerHelp commandHandlerHelp;
    private CommandHandlerShowCode commandHandlerShowCode;
    private CommandHandlerGroup commandHandlerGroup;
    private CommandHandlerAssignment commandHandlerAssignment;



    public TelegramBot(DatabaseControl databaseControl) {
        initKeyboard();
        this.commandHandlerShowCode = new CommandHandlerShowCode(this);
        this.commandHandlerStart = new CommandHandlerStart(this.commandHandlerShowCode, this);
        this.commandHandlerHelp = new CommandHandlerHelp(this, this.inlineKeyboardMarkup);
        this.commandHandlerGroup = new CommandHandlerGroup(this);
        this.commandHandlerAssignment = new CommandHandlerAssignment(this);

    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public String getBotUsername() {
        return USERNAMEBOT;
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        typeUpdate(update);
        sendAnswer(update);
    }

    /**
     * Метод изменяет состояние поля type, в зависимости от того, какой update поступает.
     */
    private synchronized void typeUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && commandWord(update.getMessage().getText()))
            this.type = MESSAGE;
        else
            this.type = OTHER;
    }

    //TODO добавлять командные слова
    /**
     * @param word поступающее слово, которое нужно проверить, является ли оно командным.
     * @return возвращает true, если слово является командой бля бота.
     */
    private Boolean commandWord(String word) {
        return word.equals("/start")
                || word.equals("/help")
                || word.equals("Мои группы")
                || word.matches("^([Сс])оздать группу: ([A-zА-я-]+)$")
                || word.matches("^([Дд])обавить в: ([A-zА-я-]+) ([a-z0-9, ]+)$")
                || word.matches("^([Вв])ыйти из группы: ([A-zА-я-]+)$")
                || word.equals("Мой код")
                || word.matches("^Дать поручение пользователю: ([a-z0-9]+)\\nДетали: ([А-яA-z0-9-_@ !,./|%$#\"'*&?+)(^;№:=]+)\\nВремя окончания поручения: (\\d{1,2}.\\d{1,2}.\\d{4}) (\\d{1,2}:\\d{1,2})$")
                || word.matches("^Дать поручение группе: ([A-zА-я-]+)\\nДетали: ([А-яA-z0-9-_@ !,./|%$#\"'*&?+)(^;№:=]+)\\nВремя окончания поручения: (\\d{1,2}.\\d{1,2}.\\d{4}) (\\d{1,2}:\\d{1,2})$");
    }

    /**
     * Метод, в зависимости от типа Update, вызывает метод его обработки.
     */
    private synchronized void sendAnswer(Update update) {
        switch (this.type) {
            case MESSAGE -> sendMessage(update);
            case OTHER -> sendOther(update);
        }
    }

    //TODO доделать весь список команд
    /**
     * Метод обрабатывает введённые текстовые команды.
     */
    private void sendMessage(Update update) {
        String message = update.getMessage().getText();
        switch (message) {
            case "/start":
                this.commandHandlerStart.commandStart(update);
                return;
            case "/help":
                this.commandHandlerHelp.commandHelp(update);
                return;
            case "Мои группы":
                this.commandHandlerGroup.commandShowGroups(update);
                return;
            case "Мой код":
                commandHandlerShowCode.commandShowCode(update);
                return;
            default:
                if (message.matches("^([Сс])оздать группу: ([A-zА-я-]+)$")){
                    this.commandHandlerGroup.createGroup(message, update);
                } else if (message.matches("^([Дд])обавить в: ([A-zА-я-]+) ([a-z0-9, ]+)$")){
                    this.commandHandlerGroup.addUsersToGroup(message, update);
                } else if (message.matches("^([Вв])ыйти из группы: ([A-zА-я-]+)$")){
                    this.commandHandlerGroup.exitFromGroup(message, update);
                } else if (message.matches("^([Дд])ать поручение пользователю: ([a-z0-9]+)\\nДетали: ([А-яA-z0-9-_@ !,./|%$#\"'*&?+)(^;№:=]+)\\nВремя окончания поручения: (\\d{1,2}.\\d{1,2}.\\d{4}) (\\d{1,2}:\\d{1,2})$")){
                    this.commandHandlerAssignment.commandGiveTaskToUser(message, update);
                } else if (message.matches("^([дД])ать поручение группе: ([A-zА-я-]+)\\nДетали: ([А-яA-z0-9-_@ !,./|%$#\"'*&?+)(^;№:=]+)\\nВремя окончания поручения: (\\d{1,2}.\\d{1,2}.\\d{4}) (\\d{1,2}:\\d{1,2})$")){
                    commandHandlerAssignment.commandGiveTaskToGroup(message, update);
                }
                return;
        }
    }


    /**
     * Метод отправляет пользователю сообщение о том, что действие нераспознанное.
     */
    @SneakyThrows
    private void sendOther(Update update) {
        execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text("Действие нераспознано. Пожалуйста, вызовите помощь командой /help").build());
    }

    /**
     * Инициализация клавиатуры для команды /help. Вызывается в конструкторе.
     */
    //TODO добавлять кнопки
    public void initKeyboard() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        /**
         * Первый ряд кнопок
         */
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        buttonsRow1.add(InlineKeyboardButton.builder().text("Мои группы").switchInlineQueryCurrentChat("Мои группы").build());
        buttonsRow1.add(InlineKeyboardButton.builder().text("Создать группу").switchInlineQueryCurrentChat("Создать группу: ИМЯ-ГРУППЫ").build());
        buttonsRow1.add(InlineKeyboardButton.builder().text("Мой код").switchInlineQueryCurrentChat("Мой код").build());
        /**
         * Второй ряд кнопок
         */
        List<InlineKeyboardButton> buttonsRow2 = new ArrayList<>();
        buttonsRow2.add(InlineKeyboardButton.builder().text("Добавить в группу").switchInlineQueryCurrentChat("Добавить в: НАЗВАНИЕ-ГРУППЫ a1234b,1abcd2,50912e").build());
        buttonsRow2.add(InlineKeyboardButton.builder().text("Выйти из группы").switchInlineQueryCurrentChat("Выйти из группы: НАЗВАНИЕ-ГРУППЫ").build());
        /**
         * Третий ряд кнопок
         */
        List<InlineKeyboardButton> buttonsRow3 = new ArrayList<>();
        buttonsRow3.add(InlineKeyboardButton.builder().text("Дать поручение пользователю").switchInlineQueryCurrentChat("Дать поручение пользователю: a1234b\nДетали: ДЕТАЛИ ПОРУЧЕНИЯ\nВремя окончания поручения: ДД.ММ.ГГГГ ЧЧ:ММ").build());
        /**
         * Четвёртый ряд кнопок
         */
        List<InlineKeyboardButton> buttonsRow4 = new ArrayList<>();
        buttonsRow4.add(InlineKeyboardButton.builder().text("Дать поручение группе").switchInlineQueryCurrentChat("Дать поручение группе: НАЗВАНИЕ-ГРУППЫ\nДетали: ДЕТАЛИ ПОРУЧЕНИЯ\nВремя окончания поручения: ДД.ММ.ГГГГ ЧЧ:ММ").build());
        /**
         * Объединение рядов
         */
        List<List<InlineKeyboardButton>> rowArrayList = new ArrayList<>();
        rowArrayList.add(buttonsRow1);
        rowArrayList.add(buttonsRow2);
        rowArrayList.add(buttonsRow3);
        rowArrayList.add(buttonsRow4);
        /**
         * Добавление рядов в объект клавиатуры
         */
        inlineKeyboardMarkup.setKeyboard(rowArrayList);
    }

    @SneakyThrows
    public static void main(String[] args) {
        DatabaseControl databaseControl = DatabaseControl.getObjectDatabaseControl();
        TelegramBot telegramBot = new TelegramBot(databaseControl);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBot);
    }
}