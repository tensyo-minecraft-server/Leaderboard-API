package moe.nmkmn.leaderboard_api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import jp.jyn.jecon.Jecon;
import moe.nmkmn.leaderboard_api.commands.LeaderBoardCommand;
import moe.nmkmn.leaderboard_api.listeners.BlockBreak;
import moe.nmkmn.leaderboard_api.listeners.BlockPlace;
import moe.nmkmn.leaderboard_api.listeners.PlayTime;
import moe.nmkmn.leaderboard_api.models.PlayerModel;
import moe.nmkmn.leaderboard_api.utils.Database;
import moe.nmkmn.leaderboard_api.utils.LeaderBoardExpansion;
import moe.nmkmn.leaderboard_api.utils.PlayerDB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

public final class Leaderboard_API extends JavaPlugin {
    private Jecon jecon;
    private DataSource dataSource;
    private static int increment = 1;
    private static boolean useMariaDbDriver = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Load Jecon
        if (!Bukkit.getPluginManager().isPluginEnabled("Jecon")) {
            getLogger().warning("Could not find Jecon! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.jecon = (Jecon) Bukkit.getPluginManager().getPlugin("Jecon");

        // Load HikariCP DataSource
        try {
            loadDataSource();
        } catch (RuntimeException e) {
            dataSource = null;
            useMariaDbDriver = true;
            loadDataSource();
        }

        try {
            // Database Initialize
            Database database = new Database(dataSource.getConnection());
            database.initialize();

            // Hook to PlaceholderAPI
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
                new LeaderBoardExpansion(this, database).register(); //
            }

            // Set Balance Status
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player: Bukkit.getServer().getOnlinePlayers()) {
                        OptionalDouble value = jecon.getRepository().getDouble(player.getUniqueId());

                        PlayerDB playerDB = new PlayerDB();

                        if (value.isPresent()) {
                            try {
                                PlayerModel playerModel = playerDB.getPlayerFromDatabase(database.connection(), player);
                                playerModel.setBalance(value.getAsDouble());
                                playerDB.update(database.connection(), playerModel);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }.runTaskTimer(this, 0, 60 * 20L);

            // Event Listeners
            getServer().getPluginManager().registerEvents(new BlockPlace(this, database), this);
            getServer().getPluginManager().registerEvents(new BlockBreak(this, database), this);
            getServer().getPluginManager().registerEvents(new PlayTime(this, database), this);

            // Command Listeners
            Objects.requireNonNull(getCommand("lb-api")).setExecutor(new LeaderBoardCommand(this, database));
        } catch (SQLException e) {
            getLogger().severe(e.getMessage());
        }
    }

    private static synchronized void increment() {
        increment++;
    }

    public void loadDataSource() {
        try {

            HikariConfig hikariConfig = new HikariConfig();

            String host = Objects.requireNonNull(getConfig().getString("database.host"));
            String port = Objects.requireNonNull(getConfig().getString("database.port"));
            String database = Objects.requireNonNull(getConfig().getString("database.database"));
            String launchOptions = Objects.requireNonNull(getConfig().getString("database.launch_options"));

            if (launchOptions.isEmpty() || !launchOptions.matches("\\?((([\\w-])+=.+)&)*(([\\w-])+=.+)")) {
                launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
            }

            hikariConfig.setDriverClassName(useMariaDbDriver ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver");
            String protocol = useMariaDbDriver ? "jdbc:mariadb" : "jdbc:mysql";
            hikariConfig.setJdbcUrl(protocol + "://" + host + ":" + port + "/" + database + launchOptions);

            String username = Objects.requireNonNull(getConfig().getString("database.user"));
            String password = Objects.requireNonNull(getConfig().getString("database.password"));

            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.addDataSourceProperty("connectionInitSql", "set time_zone = '+00:00'");

            hikariConfig.setPoolName("Leaderboard-API Connection Pool-" + increment);
            increment();

            hikariConfig.setAutoCommit(false);
            hikariConfig.setMaximumPoolSize(getConfig().getInt("database.max_connections"));
            hikariConfig.setMaxLifetime(getConfig().getInt("database.max_lifetime") * 60000L);
            hikariConfig.setLeakDetectionThreshold((getConfig().getInt("database.max_lifetime") * 60000L) + TimeUnit.SECONDS.toMillis(4L));

            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            if (e.getMessage().contains("Unknown system variable 'transaction_isolation'")) {
                throw new RuntimeException("MySQL driver is incompatible with database that is being used.", e);
            }
            throw new RuntimeException("Failed to set-up HikariCP Datasource: " + e.getMessage(), e);
        }
    }
}
