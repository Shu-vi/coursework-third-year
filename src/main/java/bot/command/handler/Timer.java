package bot.command.handler;

import bot.TelegramBot;
import database.DatabaseControl;
import entity.Assignment;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class Timer implements Runnable{
    DateTime start;
    DateTime finish;
    DateTime diff;
    Assignment assignment;
    TelegramBot telegramBot;

    public Timer(DateTime finish, Assignment assignment, TelegramBot telegramBot){
        this.start = new DateTime();
        this.finish = finish;
        this.diff = this.finish.minus(this.start.getMillis());
        this.assignment = assignment;
        this.telegramBot = telegramBot;
    }

    @Override @SneakyThrows
    public void run() {
        Thread.sleep(this.diff.getMillis());
        DatabaseControl databaseControl = DatabaseControl.getObjectDatabaseControl();
        databaseControl.setAssignmentDone(this.assignment.getId());
        String text = "Время на выполнение задачи \"" + this.assignment.getDetail() + "\" истекло.";
        this.telegramBot.execute(SendMessage.builder().text(text).chatId(assignment.getIdUserTo().toString()).build());
    }
}
