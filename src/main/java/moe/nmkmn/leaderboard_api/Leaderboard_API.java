package moe.nmkmn.leaderboard_api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import jp.jyn.jecon.Jecon;
import moe.nmkmn.leaderboard_api.commands.LeaderBoardCommand;
import moe.nmkmn.leaderboard_api.listeners.BlockBreak;
import moe.nmkmn.leaderboard_api.listeners.BlockPlace;
import moe.nmkmn.leaderboard_api.models.PlayerModel;
import moe.nmkmn.leaderboard_api.utils.Cache;
import moe.nmkmn.leaderboard_api.utils.Database;
import moe.nmkmn.leaderboard_api.utils.LeaderBoardExpansion;
import moe.nmkmn.leaderboard_api.utils.PlayerDB;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.sql.DataSource;
import java.sql.Connection;
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

        // Load Cache
        Cache cache = new Cache(this);
        cache.checkCache("blockBreak.json");
        cache.checkCache("blockPlace.json");

        // Load Jecon
        if (!Bukkit.getPluginManager().isPluginEnabled("Jecon")) {
            getLogger().warning("Could not find Jecon! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.jecon = (Jecon) Bukkit.getPluginManager().getPlugin("Jecon");

        // Hook to PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LeaderBoardExpansion(this).register();
        }

        // Load HikariCP DataSource
        try {
            loadDataSource();
        } catch (RuntimeException e) {
            dataSource = null;
            useMariaDbDriver = true;
            loadDataSource();
        }

        // Database Initialize
        try (Connection connection = getConnection()) {
            Database database = new Database(connection);
            database.initialize();
        } catch (SQLException e) {
            getLogger().severe(e.getMessage());
        }

        // Writing To Database
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerDB playerDB = new PlayerDB();

                for (Player player: Bukkit.getServer().getOnlinePlayers()) {
                    try (Connection connection = getConnection()) {
                        // Writing Cache
                        PlayerModel playerModel = playerDB.getPlayerFromDatabase(connection, player);

                        Long blockBreak = cache.getCache("blockBreak", player.getUniqueId().toString());
                        Long blockPlace = cache.getCache("blockPlace", player.getUniqueId().toString());

                        if (blockBreak != null) {
                            playerModel.setBlockBreak(playerModel.getBlockBreak() + blockBreak);
                        }

                        if (blockPlace != null) {
                            playerModel.setBlockPlace(playerModel.getBlockPlace() + blockPlace);
                        }

                        cache.saveCache(player, "blockBreak", 0);
                        cache.saveCache(player, "blockPlace", 0);

                        // Writing PlayTime
                        playerModel.setPlayTime(player.getStatistic(Statistic.PLAY_ONE_MINUTE));

                        // Writing Money
                        OptionalDouble value = jecon.getRepository().getDouble(player.getUniqueId());

                        if (value.isPresent()) {
                            playerModel.setBalance(value.getAsDouble());
                        }

                        // Writing lastName
                        playerModel.setLastName(player.getName());

                        playerDB.update(connection, playerModel);
                    } catch (Exception e) {
                        getLogger().severe(e.getMessage());
                    }
                }
            }
        }.runTaskTimer(this, 0, 60 * 20L);

        // Event Listeners
        getServer().getPluginManager().registerEvents(new BlockPlace(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreak(this), this);

        // Command Listeners
        Objects.requireNonNull(getCommand("lb-api")).setExecutor(new LeaderBoardCommand(this));
    }

    public synchronized Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (!connection.isValid(5)) {
            connection.close();
            try {
                return getConnection();
            } catch (StackOverflowError databaseHasGoneDown) {
                throw new RuntimeException("Valid connection could not be fetched (Is MySQL down?) - attempted until StackOverflowError occurred.", databaseHasGoneDown);
            }
        }
        if (connection.getAutoCommit()) connection.setAutoCommit(false);
        return connection;
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

            hikariConfig.addDataSourceProperty("autoReconnect", true);
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
