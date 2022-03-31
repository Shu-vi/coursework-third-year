package database;

import entity.Group;
import entity.User;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import properties.GetProperties;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseControl {
    private static final String db_username = GetProperties.getDBUsername();
    private static final String db_url = GetProperties.getDBUrl();
    private static final String db_password = GetProperties.getDBPassword();
    private Connection connection;
    /**
     * Singleton класс
     */
    private volatile static DatabaseControl databaseControl;

    private DatabaseControl(){}

    /**
     * @return возвращает единственный экзэмпляр класса.
     */
    public static DatabaseControl getObjectDatabaseControl(){
        if (databaseControl == null){
            synchronized (DatabaseControl.class){
                if (databaseControl == null){
                    databaseControl = new DatabaseControl();
                }
            }
        }
        return databaseControl;
    }

    @SneakyThrows
    private void connectToDB() {
        this.connection = DriverManager.getConnection(db_url, db_username, db_password);
    }

    @SneakyThrows
    private void disconnectBD() {
        this.connection.close();
    }

    /**
     * @return возвращает список всех пользователей, которые когда-либо воспользовались ботом.
     */
    @SneakyThrows
    public ArrayList<User> getUsers() {
        connectToDB();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("select * from t_user");
        ArrayList<User> users = new ArrayList<>();
        while (result.next()) {
            users.add(new User(
                    result.getLong("id"),
                    result.getString("name"),
                    result.getString("second_name"),
                    result.getString("code")
            ));
        }
        disconnectBD();
        return users;
    }

    /**
     * Возвращает пользователя по id.
     */
    @SneakyThrows
    public User getUserById(Long id) {
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("select * from t_user where id = ?");
        statement.setLong(1, id);
        ResultSet result = statement.executeQuery();
        User user = null;
        while (result.next()) {
            user = new User(
                    result.getLong("id"),
                    result.getString("name"),
                    result.getString("second_name"),
                    result.getString("code")
            );
        }
        disconnectBD();
        return user;
    }


    /**
     * @param idUser получает на вход.
     * @return возвращает лист со списком групп, в которых состоит пользователь с id idUser.
     */
    @SneakyThrows
    public ArrayList<Group> getGroupsByUserId(Long idUser) {
        connectToDB();
        ArrayList<Group> groups = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement("select t_group.id, name from t_group, groups where groups.id_user = ? AND t_group.id = groups.id_group");
        preparedStatement.setLong(1, idUser);
        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            groups.add(new Group(
                    result.getLong("id"),
                    result.getString("name")
            ));
        }
        disconnectBD();
        return groups;
    }

    @SneakyThrows
    public void createGroup(String name){
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("insert into t_group(name) values (?)");
        statement.setString(1, name);
        statement.execute();
        disconnectBD();
    }

    @SneakyThrows
    public void deleteUserFromGroupById(Long userId, String groupName){
        Long groupId = getGroupIdByGroupName(groupName);
        connectToDB();
        PreparedStatement preparedStatement = connection.prepareStatement("delete from groups where groups.id_user = ? and groups.id_group = ?");
        preparedStatement.setLong(1, userId);
        preparedStatement.setLong(2, groupId);
        preparedStatement.execute();
        disconnectBD();
    }

    @SneakyThrows
    public void addToGroup(String userCode, String groupName){
        Long userId = getUserIdByUserCode(userCode);
        Long groupId = getGroupIdByGroupName(groupName);
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("insert into groups(id_user, id_group) values (?, ?)");
        statement.setLong(1, userId);
        statement.setLong(2, groupId);
        statement.execute();
        disconnectBD();
    }

    @SneakyThrows
    public String getUserCodeByUserId(Long id){
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("select code from t_user where t_user.id = ?");
        statement.setLong(1, id);
        ResultSet result = statement.executeQuery();
        result.next();
        String code = result.getString("code");
        disconnectBD();
        return code;
    }

    @SneakyThrows
    public Long getGroupIdByGroupName(String groupName){
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("select id from t_group where name = ?");
        statement.setString(1, groupName);
        ResultSet result = statement.executeQuery();
        result.next();
        Long id = result.getLong("id");
        disconnectBD();
        return id;
    }

    @SneakyThrows
    public ArrayList<Long> getUsersByGroupName(String groupName){
        Long groupId = getGroupIdByGroupName(groupName);
        connectToDB();
        ArrayList<Long> usersId = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement("select id_user from groups where id_group = ?");
        preparedStatement.setLong(1, groupId);
        ResultSet result = preparedStatement.executeQuery();
        while (result.next()){
            usersId.add(result.getLong("id_user"));
        }
        disconnectBD();
        return usersId;
    }

    @SneakyThrows
    private Long getUserIdByUserCode(String userCode){
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("select id from t_user where code = ?");
        statement.setString(1, userCode);
        ResultSet result = statement.executeQuery();
        result.next();
        Long id = result.getLong("id");
        disconnectBD();
        return id;
    }

    @SneakyThrows
    public User getUserByCode(String code){
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("select * from t_user where t_user.code = ?");
        statement.setString(1, code);
        ResultSet result = statement.executeQuery();
        User user = null;
        while (result.next()) {
            user = new User(result.getLong("id"), result.getString("name"), result.getString("second_name"), result.getString("code"));
        }
        disconnectBD();
        return user;
    }

    @SneakyThrows
    public void createAssignment(Long idUserFrom, Long idUserTo, Long idGroupTo, String detail, DateTime dateTime){
        connectToDB();
        Timestamp timestamp = new Timestamp(dateTime.getMillis());
        PreparedStatement statement = connection.prepareStatement("insert into assignment(id_user_from, id_user_to, id_group_to, detail, time, report, edits, done) values (?, ?, ?, ?, ?, \' \', \' \', false)");
        statement.setLong(1, idUserFrom);
        if (idUserTo==null)
            statement.setNull(2, Types.BIGINT);
        else
            statement.setLong(2, idUserTo);
        if (idGroupTo==null)
            statement.setNull(3, Types.BIGINT);
        else
            statement.setLong(3, idGroupTo);
        statement.setString(4, detail);
        statement.setTimestamp(5, timestamp);
        statement.execute();
        disconnectBD();
    }

    @SneakyThrows
    public Long getLastIdFromAssigment(){
        connectToDB();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select max(id) from assignment");
        resultSet.next();
        Long id = resultSet.getLong(1);
        disconnectBD();
        return id;
    }

    @SneakyThrows
    public synchronized void setAssignmentDone(Long id){
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("update assignment set open = FALSE where id = ?");
        statement.setLong(1, id);
        statement.execute();
        disconnectBD();
    }

    @SneakyThrows
    public void addUser(Long id, String name, String secondName, String code) {
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("insert into t_user(id, name , second_name, code) values (?, ?, ?, ?)");
        statement.setLong(1, id);
        statement.setString(2, name);
        statement.setString(3, secondName);
        statement.setString(4, code);
        statement.executeUpdate();
        disconnectBD();
    }
}
