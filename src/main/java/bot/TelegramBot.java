package bot;

import codeGenerator.Code;
import database.DatabaseControl;
import entity.Group;
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
            this.type = MESSAGE;
        else if (update.hasCallbackQuery())
            this.type = CALLBACK;
        else
            this.type = OTHER;
    }

    //TODO добавлять командные слова
    /**
     * @param word поступаемое слово, которое нужно проверить, является ли оно командным.
     * @return возвращает true, если слово является командой бля бота.
     */
    private Boolean commandWord(String word) {
        return word.equals("/start")
                || word.equals("/help")
                || word.equals("Мои группы")
                || word.matches("^([Сс])оздать группу ([A-Za-zа-яА-Я-]+)$")
                || word.matches("^([Дд])обавить в ([A-Za-zа-яА-Я-]+) участников ([A-Za-z0-9,]+)$");
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

    //TODO доделать весь список команд
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
                commandShowGroups(update);
                return;
            default:
                if (message.matches("^([Сс])оздать группу ([A-Za-zа-яА-Я-]+)$")){
                    createGroup(message, update);
                } else if (message.matches("^([Дд])обавить в ([A-Za-zа-яА-Я-]+) участников ([A-Za-z0-9,]+)$")){
                    System.out.println("Точка1");
                    addUsersToGroup(message, update);
                }
                return;
        }
    }

    /**
     * Метод создаёт в БД группу с заданным именем.
     */
    @SneakyThrows
    private void createGroup(String message, Update update){//15 обрезать
        String subMessage = message.substring(15);
        String groupName = getGroupNameFromString(subMessage);
        databaseControl.createGroup(groupName);
        execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text("Группа успешно создана").build());
    }

    /**
     * Метод добавляет указанных пользователей в указанную группу
     */
    private void addUsersToGroup(String message, Update update){//11 первых обрезать
        String subMessage = message.substring(11);
        String groupName = getGroupNameFromString(subMessage);
        ArrayList<String> usersCode = getUsersCode(subMessage);
        System.out.println(groupName);
        usersCode.stream().forEach(e-> System.out.println(e));
        for (int i = 0; i < usersCode.size(); i++) {
            databaseControl.addToGroup(usersCode.get(i), groupName);
            sendMessageAboutToGroupAdd(update, groupName, usersCode.get(i));
        }
    }

    /**
     * Метод отправляет пользователям сообщение о том, что их добавили в группу и о том, кто их добавил
     */
    @SneakyThrows
    private void sendMessageAboutToGroupAdd(Update update, String groupName, String userCode){
        String chatId = databaseControl.getUserByCode(userCode).getId().toString();
        String text = "Здравствуйте!\nПользователь " + update.getMessage().getFrom().getFirstName() + " с личным кодом " +
                databaseControl.getUserCodeByUserId(update.getMessage().getChatId()) + " добавил Вас в группу " + groupName;
        execute(SendMessage.builder().chatId(chatId).text(text).build());
    }

    /**
     * Извлекает из строки коды пользователей, которых нужно добавить в группу.
     */
    private ArrayList<String> getUsersCode(String sumMessage){
        String code = "";
        sumMessage += ",";
        ArrayList<String> codes = new ArrayList<>();
        int i = 0;
        while (sumMessage.charAt(i) != ' ')
            i++;
        i++;
        while (sumMessage.charAt(i) != ' ')
            i++;
        i++;
        while (i < sumMessage.length()){
            if (sumMessage.charAt(i)!=','){
                code += sumMessage.charAt(i);
            } else{
                codes.add(code);
                code = "";
            }
            i++;
        }
        return codes;
    }


    /**
     * Извлекает из строки название группы.
     */
    private String getGroupNameFromString(String message){
        String subMessage = "";
        int i = 0;
        while (i < message.length() && message.charAt(i) != ' '){
            subMessage += message.charAt(i);
            i++;
        }
        return subMessage;
    }

    //TODO добавлять сюда список с командами по мере их появления
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
     * Обработчик срабатывает при команде "Мои группы".
     */
    @SneakyThrows
    private void commandShowGroups(Update update) {
        ArrayList<Group> groups = databaseControl.getGroupsByUserId(update.getMessage().getChatId());
        String message = !groups.isEmpty() ? getMessageUserGroups(groups) : getMessageUserGroupsNotFound();
        execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text(message).build());
    }

    /**
     * @param groups поступает список групп пользователя
     * @return возвращается отформатированная строка со всеми группами пользователя
     */
    private String getMessageUserGroups(ArrayList<Group> groups){
        String message = "Ваши группы:\n";
        for (int i = 0; i < groups.size(); i++){
            message += (i+1) + ") " + groups.get(i).getName() + "\n";
        }
        return message;
    }

    /**
     * @return возвращается строка, в которой говорится, что пользователь не состоит ни в одной группе.
     */
    private String getMessageUserGroupsNotFound(){
        return "Вы не состоите ни в одной группе.";
    }

    /**
     * Действия, которые будут выполняться при команде /start.
     * Добавляет юзера в БД, если его там ещё нет.
     * Пишет юзеру приветствие.
     */
    private void commandStart(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        Code codeGenerator = new Code();
        if (!isUserExist(userId)) {
            String name = update.getMessage().getFrom().getFirstName();
            String secondName = update.getMessage().getFrom().getUserName();
            String code = codeGenerator.getUniqueCode();
            databaseControl.addUser(userId, name, secondName, code);
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
        DatabaseControl databaseControl = DatabaseControl.getObjectDatabaseControl();
        TelegramBot telegramBot = new TelegramBot(databaseControl);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBot);
    }
}//TODO Создать и добавить в группу, выйти из группы, удалить группы(если в ней никого нет), дать поручение пользователю, дать поручение группе.