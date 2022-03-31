package bot.command.handler;

import bot.TelegramBot;
import database.DatabaseControl;
import entity.Group;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

public class CommandHandlerGroup {
    private TelegramBot telegramBot;
    private DatabaseControl databaseControl;

    public CommandHandlerGroup(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
        this.databaseControl = DatabaseControl.getObjectDatabaseControl();
    }

    /**
     * Метод создаёт в БД группу с заданным именем.
     */
    @SneakyThrows
    public void createGroup(String message, Update update){
        /**
         * Обрезка сообщения до момента, пока не начнётся название группы. Обрезанная подстрака и будет названием
         */
        String groupName = message.substring(16);
        databaseControl.createGroup(groupName);
        String text = "Группа c названием " + groupName + " успешно создана";
        this.telegramBot.execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text(text).build());
    }

    /**
     * Метод добавляет указанных пользователей в указанную группу
     */
    public void addUsersToGroup(String message, Update update){//^([Дд])обавить в: ([A-zА-я-]+) ([a-z0-9, ]+)$
        /**
         * Обрезание строки до формата "НАЗВАНИЕ-ГРУППЫ КОДЫ"
         */
        String subMessage = message.substring(12);
        String groupName = getGroupNameFromString(subMessage);
        ArrayList<String> usersCode = getUsersCode(subMessage);
        for (int i = 0; i < usersCode.size(); i++) {
            databaseControl.addToGroup(usersCode.get(i), groupName);
            sendMessageAboutToGroupAdd(update, groupName, usersCode.get(i));
        }
    }

    public void exitFromGroup(String message, Update update){
        /**
         * Обрезаем сообщение до названия группы
         */
        String groupName = message.substring(17);
        databaseControl.deleteUserFromGroupById(update.getMessage().getChatId(), groupName);
    }

    /**
     * Метод отправляет пользователям сообщение о том, что их добавили в группу и о том, кто их добавил
     */
    @SneakyThrows
    private void sendMessageAboutToGroupAdd(Update update, String groupName, String userCode){
        String chatId = databaseControl.getUserByCode(userCode).getId().toString();
        String text = "Здравствуйте!\nПользователь " + update.getMessage().getFrom().getFirstName() + " с личным кодом " +
                databaseControl.getUserCodeByUserId(update.getMessage().getChatId()) + " добавил Вас в группу " + groupName;
        this.telegramBot.execute(SendMessage.builder().chatId(chatId).text(text).build());
    }

    /**
     * Извлекает из строки вида "НАЗВАНИЕ-ГРУППЫ КОДЫ" коды пользователей.
     */
    private ArrayList<String> getUsersCode(String message){
        String code = "";
        message += ",";
        ArrayList<String> codes = new ArrayList<>();
        int i = 0;
        while (message.charAt(i) != ' ')
            i++;
        i++;
        while (i < message.length()){
            if (message.charAt(i)!=',' && message.charAt(i)!=' '){
                code += message.charAt(i);
            } else if (message.charAt(i)==','){
                codes.add(code);
                code = "";
            }
            i++;
        }
        return codes;
    }

    /**
     * Извлекает из строки вида "НАЗВАНИЕ-ГРУППЫ КОДЫ" название группы.
     */
    private String getGroupNameFromString(String message){
        String subMessage = "";
        int i = 0;
        while (message.charAt(i) != ' '){
            subMessage += message.charAt(i);
            i++;
        }
        return subMessage;
    }

    /**
     * Обработчик срабатывает при команде "Мои группы".
     */
    @SneakyThrows
    public void commandShowGroups(Update update) {
        ArrayList<Group> groups = databaseControl.getGroupsByUserId(update.getMessage().getChatId());
        String message = !groups.isEmpty() ? getMessageUserGroups(groups) : getMessageUserGroupsNotFound();
        this.telegramBot.execute(SendMessage.builder().chatId(update.getMessage().getChatId().toString()).text(message).build());
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
}
