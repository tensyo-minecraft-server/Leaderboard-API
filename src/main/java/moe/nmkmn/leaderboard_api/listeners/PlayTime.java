package moe.nmkmn.leaderboard_api.listeners;

import moe.nmkmn.leaderboard_api.Leaderboard_API;
import moe.nmkmn.leaderboard_api.models.PlayerModel;
import moe.nmkmn.leaderboard_api.utils.Database;
import moe.nmkmn.leaderboard_api.utils.PlayerDB;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayTime implements Listener {
    private final Leaderboard_API plugin;
    private final Database database;

    public PlayTime(Leaderboard_API plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    private void setPlayTime(Player player) {
        try {
            PlayerDB playerDB = new PlayerDB();

            PlayerModel playerModel = playerDB.getPlayerFromDatabase(database.connection(), player);
            playerModel.setPlayTime(player.getStatistic(Statistic.PLAY_ONE_MINUTE));
            playerDB.update(database.connection(), playerModel);
        } catch (SQLException err) {
            plugin.getLogger().severe(err.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        setPlayTime(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        setPlayTime(e.getPlayer());
    }
}
