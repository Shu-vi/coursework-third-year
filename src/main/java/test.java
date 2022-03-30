import database.DatabaseControl;
import org.joda.time.DateTime;

public class test {
    public static void main(String[] args) {
//        DateTime dateTime = new DateTime(2022, 3, 30, 12, 43, 0);//Пользователь ввёл дедлайн
//        Long idUserTo = 641955034L;
//        Long idUserFrom = 641955034L;
//        String detail = "Покушац";
//        //добавляем в базу
//        System.out.println("Пробую добавить в базу");
        DatabaseControl databaseControl = DatabaseControl.getObjectDatabaseControl();
//        databaseControl.createAssignment(idUserFrom, idUserTo, null, detail, dateTime);
        Long id = databaseControl.getLastIdFromAssigment();
        System.out.println(id);
    }
}
