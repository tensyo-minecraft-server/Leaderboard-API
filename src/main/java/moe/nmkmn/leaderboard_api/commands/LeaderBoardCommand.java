package moe.nmkmn.leaderboard_api.commands;

import moe.nmkmn.leaderboard_api.Leaderboard_API;
import moe.nmkmn.leaderboard_api.models.PlayerModel;
import moe.nmkmn.leaderboard_api.utils.PlayerDB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class LeaderBoardCommand implements CommandExecutor {
    public Leaderboard_API plugin;

    public LeaderBoardCommand(Leaderboard_API plugin) {
        this.plugin = plugin;
    }

    private void setValue(UUID uuid, int value, boolean place, boolean subtract) throws SQLException {
        PlayerDB playerDB = new PlayerDB();

        try (Connection connection = plugin.getConnection()) {
            PlayerModel playerModel = playerDB.getUUIDByDatabase(connection, uuid);
            if (subtract) {
                if (!place) {
                    playerModel.setBlockBreak(playerModel.getBlockBreak() - value);
                } else {
                    playerModel.setBlockPlace(playerModel.getBlockPlace() - value);
                }
            } else {
                if (!place) {
                    playerModel.setBlockBreak(playerModel.getBlockBreak() + value);
                } else {
                    playerModel.setBlockPlace(playerModel.getBlockPlace() + value);
                }
            }
            playerDB.update(connection, playerModel);
        } catch (SQLException e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lb-api")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                sender.sendMessage("運営用サーバーで聞いて");
            } else {
                if (args[0].equalsIgnoreCase("block")) {
                    if (args.length == 5) {
                        boolean subtract = args[2].equalsIgnoreCase("remove");
                        if (args[1].equalsIgnoreCase("break")) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(args[3]);
                            int value = Integer.parseInt(args[4]);

                            try {
                                setValue(player.getUniqueId(), value, false, subtract);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                            if (subtract) {
                                sender.sendMessage(player.getName() + "にblockBreakを" + value + "個" + "引きました。");
                            } else {
                                sender.sendMessage(player.getName() + "にblockBreakを" + value + "個" + "足しました。");
                            }
                        } else if (args[1].equalsIgnoreCase("place")) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(args[3]);
                            int value = Integer.parseInt(args[4]);

                            try {
                                setValue(player.getUniqueId(), value, true, subtract);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }

                            if (subtract) {
                                sender.sendMessage(player.getName() + "にblockPlaceを" + value + "個" + "引きました。");
                            } else {
                                sender.sendMessage(player.getName() + "にblockPlaceを" + value + "個" + "足しました。");
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
