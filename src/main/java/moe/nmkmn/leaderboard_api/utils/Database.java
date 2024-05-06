package moe.nmkmn.leaderboard_api.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public record Database(Connection connection) {
    public void initialize() throws SQLException {
        Statement statement = connection().createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS Player (uuid varchar(36) primary key, lastName text, balance double, blockBreak long, blockPlace long, playTime long)");
        statement.close();
    }
}
