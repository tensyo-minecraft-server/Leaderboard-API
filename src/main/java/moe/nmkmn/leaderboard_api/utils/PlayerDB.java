package moe.nmkmn.leaderboard_api.utils;

import moe.nmkmn.leaderboard_api.models.PlayerModel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDB {
    public void create(Connection connection, PlayerModel playerModel) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Player(uuid, lastName, balance, blockBreak, blockPlace, playTime) VALUES (?, ?, ?, ?, ?, ?)"
        );

        statement.setString(1, playerModel.getUUID());
        statement.setString(2, playerModel.getLastName());
        statement.setDouble(3, playerModel.getBalance());
        statement.setLong(4, playerModel.getBlockBreak());
        statement.setLong(5, playerModel.getBlockPlace());
        statement.setLong(6, playerModel.getPlayTime());

        statement.executeUpdate();
        connection.commit();
        statement.close();
    }

    public void update(Connection connection, PlayerModel playerModel) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE Player SET lastName = ?, balance = ?, blockBreak = ?, blockPlace = ?, playTime = ? WHERE uuid = ?"
        );

        statement.setString(1, playerModel.getLastName());
        statement.setDouble(2, playerModel.getBalance());
        statement.setLong(3, playerModel.getBlockBreak());
        statement.setLong(4, playerModel.getBlockPlace());
        statement.setLong(5, playerModel.getPlayTime());
        statement.setString(6, playerModel.getUUID());

        statement.executeUpdate();
        connection.commit();
        statement.close();
    }

    public PlayerModel findByUUID(Connection connection, UUID uuid) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM Player WHERE uuid = ?");
        statement.setString(1, uuid.toString());

        ResultSet resultSet = statement.executeQuery();

        PlayerModel playerModel;

        if (resultSet.next()) {
            playerModel = new PlayerModel(resultSet.getString("uuid"), resultSet.getString("lastName"), resultSet.getDouble("balance"), resultSet.getLong("blockBreak"), resultSet.getLong("blockPlace"), resultSet.getLong("playTime"));

            statement.close();

            return playerModel;
        }

        statement.close();

        return null;
    }

    public PlayerModel getUUIDByDatabase(Connection connection, UUID uuid) throws SQLException {
        PlayerDB playerDB = new PlayerDB();
        PlayerModel playerModel = playerDB.findByUUID(connection, uuid);

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if (playerModel == null) {
            playerModel = new PlayerModel(uuid.toString(), player.getName(), 0.0, 0, 0, 0);
            playerDB.create(connection, playerModel);
        }

        return playerModel;
    }
}
