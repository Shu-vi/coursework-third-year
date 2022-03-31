package bot.command.handler;

import bot.TelegramBot;
import database.DatabaseControl;
import entity.Assignment;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

public class CommandHandlerAssignment {
    private TelegramBot telegramBot;
    private DatabaseControl databaseControl;

    public  CommandHandlerAssignment(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
        this.databaseControl = DatabaseControl.getObjectDatabaseControl();
    }

    //TODO
    @SneakyThrows
    public void commandGiveTaskToUser(String message, Update update){
        //Обрезаем до момента, пока не начнётся код пользователя
        String subMessage = message.substring(29);
        String code = getCode(subMessage);
        //Обрезаем до момента, пока не начнутся детали поручения
        subMessage = message.substring(44);
        String detail = getDetails(subMessage);
        DateTime deadline = getDeadline(subMessage);//Пользователь ввёл дедлайн
        Long idUserTo = databaseControl.getUserByCode(code).getId();
        Long idUserFrom = update.getMessage().getChatId();
        //добавляем в базу
        databaseControl.createAssignment(idUserFrom, idUserTo, null, detail, deadline);
        //вытаскиваем айди поручения из базы
        Long idAssignment = databaseControl.getLastIdFromAssigment();
        Timer timer = new Timer(
                new Assignment(
                        idAssignment, idUserFrom, idUserTo, detail, deadline, "", "", false, true
                ),
                this.telegramBot
        );
        new Thread(timer).start();
        String text = "Вам дали следующее поручение:\n" + detail + "\nОт пользователя " + update.getMessage().getFrom().getFirstName() + " с кодом " + databaseControl.getUserCodeByUserId(idUserFrom);
        this.telegramBot.execute(SendMessage.builder().chatId(idUserTo.toString()).text(text).build());
    }//Пользователь может сдавать задачи, указывая айди

    /**
     *
     * @param message строка вида "a1234b\nДетали..."
     * @return код пользователя
     */
    private String getCode(String message){
        String code = "";
        int i = 0;
        while (message.charAt(i)!='\n'){
            code += message.charAt(i);
            i++;
        }
        return code;
    }

    /**
     * @param message строка вида "ДЕТАЛИ ПОРУЧЕНИЯ\n..."
     * @return ДЕТАЛИ ПОРУЧЕНИЯ
     */
    private String getDetails(String message){
        String details = "";
        int i = 0;
        while (message.charAt(i)!='\n'){
            details += message.charAt(i);
            i++;
        }
        return details;
    }

    /**
     * @param message строка вида "ДЕТАЛИ ПОРУЧЕНИЯ\nВремя окончания поручения: ДД.ММ.ГГГГ ЧЧ:ММ"
     * @return DateTime с заданным пользователем датой
     */
    private DateTime getDeadline(String message){
        String year = "", month = "", day = "", hour = "", minute = "";
        int i = 0;
        message += ":";
        while (message.charAt(i)!='\n')
            i++;
        //Переводим указатель до момента, пока не появится дата
        i += 28;
        //Считываем дни
        while (message.charAt(i)!='.'){
            day += message.charAt(i);
            i++;
        }
        i++;
        //Считываем месяцы
        while (message.charAt(i)!='.'){
            month += message.charAt(i);
            i++;
        }
        i++;
        //Считываем года
        while (message.charAt(i)!=' '){
            year += message.charAt(i);
            i++;
        }
        i++;
        //Считываем часы
        while (message.charAt(i)!=':'){
            hour += message.charAt(i);
            i++;
        }
        i++;
        //Считываем минуты
        while (message.charAt(i)!=':'){
            minute += message.charAt(i);
            i++;
        }
        Integer dayInt = Integer.parseInt(day);
        Integer hourInt = Integer.parseInt(hour);
        Integer yearInt = Integer.parseInt(year);
        Integer monthInt = Integer.parseInt(month);
        Integer minuteInt = Integer.parseInt(minute);
        DateTime deadline = new DateTime(yearInt, monthInt, dayInt, hourInt, minuteInt);
        return deadline;
    }

    /**
     * Даёт поручение группе и рассылает оповещение пользователям
     */
    @SneakyThrows
    public void commandGiveTaskToGroup(String message, Update update){
        //Обрезаем до момента, пока не начнётся название группы
        String subMessage = message.substring(23);
        String groupName = getGroupName(subMessage);
        Long groupId = databaseControl.getGroupIdByGroupName(groupName);
        String detail = getDetail(subMessage);
        /**
         * Обрезаем строку до вида "ДЕТАЛИ ПОРУЧЕНИЯ\nВремя окончания поручения: ДД.ММ.ГГГГ ЧЧ:ММ"
         */
        int i = 0;
        while (subMessage.charAt(i)!='\n')
            i++;
        i++;
        while (subMessage.charAt(i)!=' ')
            i++;
        i++;
        subMessage = subMessage.substring(i);
        DateTime deadline = getDeadline(subMessage);//Пользователь ввёл дедлайн
        //из названия группы получаем айди всех её участников
        ArrayList<Long> usersIdTo = databaseControl.getUsersByGroupName(groupName);
        Long idUserFrom = update.getMessage().getChatId();
        //добавляем в базу
        i = 0;
        while (i < usersIdTo.size()) {
            databaseControl.createAssignment(idUserFrom, usersIdTo.get(i), groupId, detail, deadline);
            //вытаскиваем айди поручения из базы
            Long idAssignment = databaseControl.getLastIdFromAssigment();
            Timer timer = new Timer(
                    new Assignment(
                            idAssignment, idUserFrom, usersIdTo.get(i), detail, deadline, "", "", false, groupId, true
                    ),
                    this.telegramBot
            );
            new Thread(timer).start();
            String text = "Вашей группе \"" + groupName + "\" дали следующее поручение:\n" + detail + "\nОт пользователя " + update.getMessage().getFrom().getFirstName() + " с кодом " + databaseControl.getUserCodeByUserId(idUserFrom);
            this.telegramBot.execute(SendMessage.builder().chatId(usersIdTo.get(i).toString()).text(text).build());
            i++;
        }
    }

    /**
     * @param message строка вида "НАЗВАНИЕ-ГРУППЫ\n..."
     * @return возвращает "НАЗВАНИЕ-ГРУППЫ"
     */
    private String getGroupName(String message){
        String subMessage = "";
        int i = 0;
        while (message.charAt(i)!='\n'){
            subMessage += message.charAt(i);
            i++;
        }
        return subMessage;
    }

    /**
     * @param message строка вида "НАЗВАНИЕ-ГРУППЫ\nДетали: ОПИСАНИЕ ДЕТАЛЕЙ\n..."
     * @return возвращает "ОПИСАНИЕ ДЕТАЛЕЙ"
     */
    private String getDetail(String message){
        String detail = "";
        int i = 0;
        while (message.charAt(i)!='\n')
            i++;
        i++;
        while (message.charAt(i)!=' ')
            i++;
        i++;
        while (message.charAt(i)!='\n'){
            detail += message.charAt(i);
            i++;
        }
        return detail;
    }


}
