package moe.nmkmn.leaderboard_api.listeners;

import moe.nmkmn.leaderboard_api.Leaderboard_API;
import moe.nmkmn.leaderboard_api.models.PlayerModel;
import moe.nmkmn.leaderboard_api.utils.Database;
import moe.nmkmn.leaderboard_api.utils.PlayerDB;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.sql.SQLException;

public class BlockPlace implements Listener {
    private final Leaderboard_API plugin;
    private final Database database;

    public BlockPlace(Leaderboard_API plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (plugin.getConfig().getList("general.ignore_blocks").contains(block.getType().toString())) {
            return;
        }

        try {
            PlayerDB playerDB = new PlayerDB();

            PlayerModel playerModel = playerDB.getPlayerFromDatabase(database.connection(), player);
            playerModel.setBlockPlace(playerModel.getBlockPlace() + 1);
            playerDB.update(database.connection(), playerModel);
        } catch (SQLException err) {
            plugin.getLogger().severe(err.getMessage());
        }
    }
}
