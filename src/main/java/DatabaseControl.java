import entity.User;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseControl {
    private static final String db_username = GetProperties.getDBUsername();
    private static final String db_url = GetProperties.getDBUrl();
    private static final String db_password = GetProperties.getDBPassword();
    private Connection connection;


    @SneakyThrows
    private void connectToDB() {
        this.connection = DriverManager.getConnection(db_url, db_username, db_password);
    }

    @SneakyThrows
    private void disconnectBD() {
        this.connection.close();
    }

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
                    result.getString("second_name")
            ));
        }
        disconnectBD();
        return users;
    }

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
                    result.getString("second_name")
            );
        }
        disconnectBD();
        return user;
    }

    @SneakyThrows
    public ArrayList<String> getGroupsByUserId(Long idUser) {
        connectToDB();
        ArrayList<String> groups = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement("select name from t_group, groups where groups.id_user = ? AND t_group.id = groups.id_group");
        preparedStatement.setLong(1, idUser);
        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            groups.add(result.getString("name"));
        }
        disconnectBD();
        return groups;
    }

    @SneakyThrows
    public void addUser(Long id, String name, String secondName) {
        connectToDB();
        PreparedStatement statement = connection.prepareStatement("insert into t_user(id, name , second_name) values (?, ?, ?)");
        statement.setLong(1, id);
        statement.setString(2, name);
        statement.setString(3, secondName);
        statement.executeUpdate();
        disconnectBD();
    }
}
