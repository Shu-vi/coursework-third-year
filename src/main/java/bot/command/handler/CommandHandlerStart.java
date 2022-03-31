package bot.command.handler;

import bot.TelegramBot;
import codeGenerator.Code;
import database.DatabaseControl;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CommandHandlerStart {
    private DatabaseControl databaseControl;
    private CommandHandlerShowCode commandHandlerShowCode;
    private TelegramBot telegramBot;

    public CommandHandlerStart(CommandHandlerShowCode commandHandlerShowCode, TelegramBot telegramBot){
        this.databaseControl = DatabaseControl.getObjectDatabaseControl();
        this.commandHandlerShowCode = commandHandlerShowCode;
        this.telegramBot = telegramBot;
    }

    /**
     * Действия, которые будут выполняться при команде /start.
     * Добавляет юзера в БД, если его там ещё нет.
     * Пишет юзеру приветствие и сообщает его персональный код.
     */
    @SneakyThrows
    public void commandStart(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        Code codeGenerator = new Code();
        if (!isUserExist(userId)) {
            String name = update.getMessage().getFrom().getFirstName();
            String secondName = update.getMessage().getFrom().getUserName();
            String code = codeGenerator.getUniqueCode();
            databaseControl.addUser(userId, name, secondName, code);
        }
        sendGreeting(update);
        commandHandlerShowCode.commandShowCode(update);
    }

    /**
     * @param id нужен для запроса в бд, чтобы проверить, нет ли юзера с данным id уже в базе данных.
     * @return возвращает true, если пользователь уже есть в базе данных.
     */
    private Boolean isUserExist(Long id) {
        return databaseControl.getUserById(id) != null;
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
        this.telegramBot.execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text(messageText).build());
    }
}
