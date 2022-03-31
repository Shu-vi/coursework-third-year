package bot.command.handler;

import bot.TelegramBot;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class CommandHandlerHelp {
    private TelegramBot telegramBot;
    private InlineKeyboardMarkup inlineKeyboardMarkup;

    public CommandHandlerHelp(TelegramBot telegramBot, InlineKeyboardMarkup inlineKeyboardMarkup){
        this.telegramBot = telegramBot;
        this.inlineKeyboardMarkup = inlineKeyboardMarkup;
    }


    //TODO добавлять сюда список с командами по мере их появления
    /**
     *Обработчик срабатывает при команде /help.
     * Выводит небольшую справку с прикреплённой клавиатурой.
     */
    @SneakyThrows
    public void commandHelp(Update update) {
        String messageText = "Доступные команды:\n" +
                "1) \"Мои группы\" - выводит группы, в которые вас добавили\n" +
                "2) \"Создать группу: НАЗВАНИЕ-ГРУППЫ\" - создаёт группу с названием \"НАЗВАНИЕ-ГРУППЫ\". " +
                "Имя вашей группы должно быть написано кириллицей или латиницей, однако будьте внимательны, " +
                "имя группы может состоять из нескольких слов, но они должны быть разделены знаком \"-\"." +
                " Например, название группы \"Группа дизайнеров online\" является недопустимым, но " +
                "\"Группа-дизайнеров-online\" уже будет допустимым.\n" +
                "3) \"Мой код\" - выводит ваш персональный код. Можете сказать его тому, с кем будете пользоваться " +
                "данным ботом. Персональный код нужен для того, чтобы добавить пользователя в группу или дать " +
                "поручение конкретному пользователю.\n" +
                "4) \"Добавить в: НАЗВАНИЕ-ГРУППЫ КОДЫ\" - добавляет пользователей в группу. Для этого есть два условия:\n" +
                "- Вы должны знать код человека, которого хотите добавить\n" +
                "- Группа, в которую вы хотите добавить, должна быть предварительно создана\n" +
                "В данной команде \"НАЗВАНИЕ-ГРУППЫ\" - точное название Вашей группы, \"КОДЫ\" - коды пользователей" +
                ", вы можете указать код как одного пользователя, так и нескольких, разделённых запятой." +
                " Например, такой формат является недопустимым \"a3e178, 981b42\", однако два следующих " +
                "формата будут допустимыми \"156a89\" и \"156a89,a3e178,981b42\".\n" +
                "5) \"Выйти из группы: НАЗВАНИЕ-ГРУППЫ\" - позволяет Вам выйти из группы с названием \"НАЗВАНИЕ-ГРУППЫ\".";
        this.telegramBot.execute(
                SendMessage.builder()
                        .text(messageText)
                        .chatId(update.getMessage().getChatId().toString())
                        .replyMarkup(inlineKeyboardMarkup)
                        .build()
        );
    }
}
