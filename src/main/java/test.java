import database.DatabaseControl;
import org.joda.time.DateTime;

public class test {
    public static void main(String[] args) {
        String year = "2022", month = "03", day = "30", hour = "18", minute = "51";
        Integer yearInt = Integer.parseInt(year);
        Integer monthInt = Integer.parseInt(month);
        Integer dayInt = Integer.parseInt(day);
        Integer hourInt = Integer.parseInt(hour);
        Integer minuteInt = Integer.parseInt(minute);
        DateTime deadline = new DateTime(yearInt, monthInt, dayInt, hourInt, minuteInt);
        System.out.println(deadline);
//        Long idUserTo = 641955034L;
//        Long idUserFrom = 641955034L;
//        String detail = "Покушац";
//        //добавляем в базу
//        System.out.println("Пробую добавить в базу");
//        DatabaseControl databaseControl = DatabaseControl.getObjectDatabaseControl();
//        databaseControl.createAssignment(idUserFrom, idUserTo, null, detail, dateTime);
//        Long id = databaseControl.getLastIdFromAssigment();
//        System.out.println(id);
//        String str = "09\n1";
//        int i = 0;
//        System.out.println(str.length() + " Длина строки");
//        while (i<str.length()){
//            System.out.println(i + " " + str.charAt(i));
//            i++;
//        }
    }
}
