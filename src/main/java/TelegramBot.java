import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

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
     * Класс для взаимодействия с бд.
     */
    private DatabaseControl databaseControl;

    public TelegramBot(DatabaseControl databaseControl) {
        this.databaseControl = databaseControl;
        initKeyboard();
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
            this.type = Type.MESSAGE;
        else if (update.hasCallbackQuery())
            this.type = Type.CALLBACK;
        else
            this.type = Type.OTHER;
    }

    /**
     * @param word поступаемое слово, которое нужно проверить, является ли оно командным.
     * @return возвращает true, если слово является командой бля бота.
     */
    private Boolean commandWord(String word) {
        return word.equals("/start")
                || word.equals("/help")
                || word.equals("Мои группы");
    }


    /**
     * Метод, в зависимости от типа Update, вызывает метод его обработки.
     */
    private synchronized void sendAnswer(Update update) {
        switch (this.type) {
            case MESSAGE -> sendMessage(update);
            case CALLBACK -> sendCallback(update);
            case OTHER -> sendOther(update);
        }
    }

    /**
     * Метод обрабатывает введённые текстовые команды.
     */
    private void sendMessage(Update update) {
        String message = update.getMessage().getText();
        switch (message) {
            case "/start":
                commandStart(update);
                return;
            case "/help":
                commandHelp(update);
                return;
            case "Мои группы":
                commandShowGroups();
        }
    }

    /**
     *Обработчик срабатывает при команде /help.
     * Выводит небольшую справку с прикреплённой клавиатурой.
     */
    @SneakyThrows
    private void commandHelp(Update update) {
        String messageText = "Доступные команды:\n" +
                "1)\"Мои группы\" - выводит группы, в которые вас добавили\n";
        execute(
                SendMessage.builder()
                        .text(messageText)
                        .chatId(update.getMessage().getChatId().toString())
                        .replyMarkup(inlineKeyboardMarkup)
                        .build()
        );
    }

    /**
     * Обработчик сробатывает при команде "Мои группы".
     */
    private void commandShowGroups() {

    }

    /**
     * Действия, которые будут выполняться при команде /start.
     * Добавляет юзера в БД, если его там ещё нет.
     * Пишет юзеру приветствие.
     */
    private void commandStart(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        if (!isUserExist(userId)) {
            String name = update.getMessage().getFrom().getFirstName();
            String secondName = update.getMessage().getFrom().getUserName();
            databaseControl.addUser(userId, name, secondName);
        }
        sendGreeting(update);
    }

    /**
     *Метод отправляет пользователю приветствие. Вызывается в методе commandStart.
     */
    //TODO доделывать приветствие
    @SneakyThrows
    private void sendGreeting(Update update) {
        String messageText = "Здравствуйте, " + update.getMessage().getFrom().getFirstName() + ".\nЭтот бот предназначен для личного " +
                "использования, " +
                "вы можете использовать его как с семьёй, так и на работе." +
                "\nВы можете, например, дать поручение другу и следить за его выполнением. Просто начните пользоваться ботом, наберите" +
                " /help или вводите команды вручную.";
        execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text(messageText).build());
    }

    /**
     * @param id нужен для запроса в бд, чтобы проверить, нет ли юзера с данным id уже в базе данных.
     * @return возвращает true, если пользователь уже есть в базе данных.
     */
    private Boolean isUserExist(Long id) {
        return databaseControl.getUserById(id) != null;
    }

    //TODO Сделать метод колбека
    /**
     * Метод обрабатывает колбеки кнопок.
     */
    private void sendCallback(Update update) {

    }

    /**
     * Метод отправляет пользователю сообщение о том, что действие нераспознанно.
     */
    @SneakyThrows
    private void sendOther(Update update) {
        execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text("Действие нераспознанно. Пожалуйста, вызовите помощь командой /help").build());
    }

    /**
     * Инициализация клавиатуры для команды /help. Вызывается в конструкторе.
     */
    //TODO добавлять кнопки
    public void initKeyboard() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        buttonsRow1.add(InlineKeyboardButton.builder().text("Мои группы").callbackData("1").build());
        List<List<InlineKeyboardButton>> rowArrayList = new ArrayList<>();
        rowArrayList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowArrayList);
    }

    @SneakyThrows
    public static void main(String[] args) {
        DatabaseControl databaseControl = new DatabaseControl();
        TelegramBot telegramBot = new TelegramBot(databaseControl);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBot);
    }
}
