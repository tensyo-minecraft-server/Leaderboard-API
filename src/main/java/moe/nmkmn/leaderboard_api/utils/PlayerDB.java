package moe.nmkmn.leaderboard_api.utils;

import moe.nmkmn.leaderboard_api.models.PlayerModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDB {
    public void create(Connection connection, PlayerModel playerModel) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Player(uuid, balance, blockBreak, blockPlace, playTime) VALUES (?, ?, ?, ?, ?)"
        );

        statement.setString(1, playerModel.getUUID());
        statement.setDouble(2, playerModel.getBalance());
        statement.setLong(3, playerModel.getBlockBreak());
        statement.setLong(4, playerModel.getBlockPlace());
        statement.setLong(5, playerModel.getPlayTime());

        statement.executeUpdate();
        connection.commit();
        statement.close();
    }

    public void update(Connection connection, PlayerModel playerModel) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE Player SET balance = ?, blockBreak = ?, blockPlace = ?, playTime = ? WHERE uuid = ?"
        );

        statement.setDouble(1, playerModel.getBalance());
        statement.setLong(2, playerModel.getBlockBreak());
        statement.setLong(3, playerModel.getBlockPlace());
        statement.setLong(4, playerModel.getPlayTime());
        statement.setString(5, playerModel.getUUID());

        statement.executeUpdate();
        connection.commit();
        statement.close();
    }

    public PlayerModel findByUUID(Connection connection, String uuid) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM Player WHERE uuid = ?");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        PlayerModel playerModel;

        if(resultSet.next()){
            playerModel = new PlayerModel(resultSet.getString("uuid"), resultSet.getDouble("balance"), resultSet.getLong("blockBreak"), resultSet.getLong("blockPlace"), resultSet.getLong("playTime"));

            statement.close();

            return playerModel;
        }

        statement.close();

        return null;
    }

    public PlayerModel getPlayerFromDatabase(Connection connection, org.bukkit.entity.Player player) throws SQLException {
        PlayerDB playerDB = new PlayerDB();
        PlayerModel playerModel = playerDB.findByUUID(connection, player.getUniqueId().toString());

        if (playerModel == null) {
            playerModel = new PlayerModel(player.getUniqueId().toString(), 0.0, 0, 0, 0);
            playerDB.create(connection, playerModel);
        }

        return playerModel;
    }

    public PlayerModel getUUIDByDatabase(Connection connection, String UUID) throws SQLException {
        PlayerDB playerDB = new PlayerDB();
        PlayerModel playerModel = playerDB.findByUUID(connection, UUID);

        if (playerModel == null) {
            playerModel = new PlayerModel(UUID, 0.0, 0, 0,0);
            playerDB.create(connection, playerModel);
        }

        return playerModel;
    }
}
