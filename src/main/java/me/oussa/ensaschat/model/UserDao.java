package me.oussa.ensaschat.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final Connection connection;

    public UserDao() {
        connection = DBConnection.getConnection();
    }


    /**
     * Get all users from the database
     *
     * @return list of all users
     */
    public List<User> getUsers() throws SQLException {
        String query = "SELECT * FROM users";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("username"));
                user.setImage(resultSet.getBytes("image"));
                users.add(user);
            }
            return users;
        }
    }

    /**
     * Sign in a user
     *
     * @param username username
     * @param password password
     * @return the signed-in user
     */
    public User signIn(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("username"));
                user.setImage(resultSet.getBytes("image"));
                return user;
            }
        }
        return null;
    }

    /**
     * Sign up a user
     *
     * @param user user to sign up
     * @return true if the user was signed up successfully, false otherwise
     */
    public boolean signUp(User user) throws SQLException {
        if (userExists(user.getUsername())) {
            return false;
        }
        String query = "INSERT INTO users (name, username, password) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.executeUpdate();
            return true;
        }
    }

    /**
     * Check if a user exists in the database
     *
     * @param username username
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }


    /**
     * Update user
     *
     * @param user user to update
     * @return true if the user was updated successfully, false otherwise
     */

    public boolean updateUser(User user) throws SQLException {
        // update user but if a field is empty, don't update it
        String query = "UPDATE users SET name = COALESCE(NULLIF(?, ''), name), password = COALESCE(NULLIF(?, ''), password), image = COALESCE(NULLIF(?, ''), image) WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getPassword());
            if (user.getImage() != null) {
                statement.setBytes(3, user.getImage());
            }
            statement.setInt(4, user.getID());
            statement.executeUpdate();
            System.out.println(statement);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
