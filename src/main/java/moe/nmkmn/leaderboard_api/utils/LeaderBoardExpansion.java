package moe.nmkmn.leaderboard_api.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import moe.nmkmn.leaderboard_api.Leaderboard_API;
import moe.nmkmn.leaderboard_api.models.PlayerModel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaderBoardExpansion extends PlaceholderExpansion {
    private final Leaderboard_API plugin;
    private final Database database;

    public LeaderBoardExpansion(Leaderboard_API plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "leaderboard";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    private String formatTime(long time) {
        time /= 1200;
        long days = time / 1440;
        time %= 1440;
        long hours = time / 60;
        time %= 60;
        long minutes = time;

        String msg = "";
        if (days > 0) {
            msg += days + "日";
        }
        if (hours > 0) {
            msg += hours + "時間";
        }
        msg += minutes + "分";

        return msg;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] params_split = params.split("_");

        try {
            if (params_split[0].equals("block")) {
                if (params_split[1].equals("break")) {
                    if (params_split[2].equals("rank")) {
                        List<String> names = new ArrayList<>();
                        List<Long> values = new ArrayList<>();

                        try {
                            PreparedStatement statement = database.connection().prepareStatement("SELECT * FROM Player ORDER BY LPAD(blockBreak,64,0) DESC LIMIT 10");
                            ResultSet resultSet = statement.executeQuery();

                            while (resultSet.next()) {
                                names.add(Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("uuid"))).getName());
                                values.add(resultSet.getLong("blockBreak"));
                            }
                        } catch (SQLException e) {
                            return null;
                        }

                        if (params_split[4].equals("name")) {
                            if (names.size() > (Integer.parseInt(params_split[3]) - 1)) {
                                return names.get(Integer.parseInt(params_split[3]) - 1);
                            } else {
                                return "None";
                            }
                        } else if (params_split[4].equals("value")) {
                            if (values.size() > (Integer.parseInt(params_split[3]) - 1)) {
                                return values.get(Integer.parseInt(params_split[3]) - 1).toString();
                            } else {
                                return "NaN";
                            }
                        }
                    } else if (params_split[2].equals("me")) {
                        PlayerDB playerDB = new PlayerDB();

                        PlayerModel playerModel = playerDB.findByUUID(database.connection(), player.getUniqueId().toString());

                        return String.valueOf(playerModel.getBlockBreak());
                    }
                } else if (params_split[1].equals("place")) {
                    if (params_split[2].equals("rank")) {
                        List<String> names = new ArrayList<>();
                        List<Long> values = new ArrayList<>();

                        try {
                            PreparedStatement statement = database.connection().prepareStatement("SELECT * FROM Player ORDER BY LPAD(blockPlace,64,0) DESC LIMIT 10");
                            ResultSet resultSet = statement.executeQuery();

                            while (resultSet.next()) {
                                names.add(Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("uuid"))).getName());
                                values.add(resultSet.getLong("blockPlace"));
                            }
                        } catch (SQLException e) {
                            return null;
                        }

                        if (params_split[4].equals("name")) {
                            if (names.size() > (Integer.parseInt(params_split[3]) - 1)) {
                                return names.get(Integer.parseInt(params_split[3]) - 1);
                            } else {
                                return "None";
                            }
                        } else if (params_split[4].equals("value")) {
                            if (values.size() > (Integer.parseInt(params_split[3]) - 1)) {
                                return values.get(Integer.parseInt(params_split[3]) - 1).toString();
                            } else {
                                return "NaN";
                            }
                        }
                    } else if (params_split[2].equals("me")) {
                        PlayerDB playerDB = new PlayerDB();

                        PlayerModel playerModel = playerDB.findByUUID(database.connection(), player.getUniqueId().toString());

                        return String.valueOf(playerModel.getBlockPlace());
                    }
                }
            } else if (params_split[0].equals("balance")) {
                if (params_split[1].equals("rank")) {
                    List<String> names = new ArrayList<>();
                    List<Double> values = new ArrayList<>();

                    try {
                        PreparedStatement statement = database.connection().prepareStatement("SELECT * FROM Player ORDER BY LPAD(balance,64,0) DESC LIMIT 10");
                        ResultSet resultSet = statement.executeQuery();

                        while (resultSet.next()) {
                            names.add(Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("uuid"))).getName());
                            values.add(resultSet.getDouble("balance"));
                        }
                    } catch (SQLException e) {
                        return null;
                    }

                    if (params_split[3].equals("name")) {
                        if (names.size() > (Integer.parseInt(params_split[2]) - 1)) {
                            return names.get(Integer.parseInt(params_split[2]) - 1);
                        } else {
                            return "None";
                        }
                    } else if (params_split[3].equals("value")) {
                        if (values.size() > (Integer.parseInt(params_split[2]) - 1)) {
                            return String.valueOf(Math.round(values.get(Integer.parseInt(params_split[2]) - 1)));
                        } else {
                            return "NaN";
                        }
                    }
                } else if (params_split[1].equals("me")) {
                    PlayerDB playerDB = new PlayerDB();

                    PlayerModel playerModel = playerDB.findByUUID(database.connection(), player.getUniqueId().toString());

                    return String.valueOf(playerModel.getBalance());
                }
            } else if (params_split[0].equals("playtime")) {
                if (params_split[1].equals("rank")) {
                    List<String> names = new ArrayList<>();
                    List<Long> values = new ArrayList<>();

                    try {
                        PreparedStatement statement = database.connection().prepareStatement("SELECT * FROM Player ORDER BY LPAD(playTime,64,0) DESC LIMIT 10");
                        ResultSet resultSet = statement.executeQuery();

                        while (resultSet.next()) {
                            names.add(Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("uuid"))).getName());
                            values.add(resultSet.getLong("playTime"));
                        }
                    } catch (SQLException e) {
                        return null;
                    }

                    if (params_split[3].equals("name")) {
                        if (names.size() > (Integer.parseInt(params_split[2]) - 1)) {
                            return names.get(Integer.parseInt(params_split[2]) - 1);
                        } else {
                            return "None";
                        }
                    } else if (params_split[3].equals("value")) {
                        if (values.size() > (Integer.parseInt(params_split[2]) - 1)) {
                            if (params_split[4].equals("raw")) {
                                return String.valueOf(values.get(Integer.parseInt(params_split[2]) - 1));
                            } else {
                                return formatTime(values.get(Integer.parseInt(params_split[2]) - 1));
                            }
                        } else {
                            return "NaN";
                        }
                    }
                } else if (params_split[1].equals("me")) {
                    PlayerDB playerDB = new PlayerDB();
                    PlayerModel playerModel = playerDB.findByUUID(database.connection(), player.getUniqueId().toString());

                    if (params_split[2].equals("raw")) {
                        return String.valueOf(playerModel.getPlayTime());
                    } else {
                        return formatTime(playerModel.getPlayTime());
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException | SQLException e) {
            return null;
        }

        return null;
    }
}
