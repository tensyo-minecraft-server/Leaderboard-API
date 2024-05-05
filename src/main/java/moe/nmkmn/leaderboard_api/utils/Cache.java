package moe.nmkmn.leaderboard_api.utils;

import moe.nmkmn.leaderboard_api.Leaderboard_API;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cache {
    private final Leaderboard_API plugin;

    public Cache(Leaderboard_API plugin) {
        this.plugin = plugin;
    }

    public void checkCache(String name) {
        File pluginDirectory = new File(plugin.getDataFolder() + "/cache/");
        if (!pluginDirectory.exists()) {
            boolean success = pluginDirectory.mkdirs();

            if (!success) {
                plugin.getLogger().warning("The directory could not be created successfully.");
            }
        }

        File cacheFile = new File(plugin.getDataFolder() + "/cache/" + name);
        if (!cacheFile.exists()) {
            try {
                FileWriter writer = new FileWriter(cacheFile.getAbsoluteFile());
                writer.write((new JSONArray().toJSONString()));
                writer.close();
            } catch (IOException e) {
                plugin.getLogger().severe(e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void saveCache(Player player, String name ,long value) {
        JSONObject target = new JSONObject();
        target.put("uuid", player.getUniqueId().toString());
        target.put("lastName", player.getName());
        target.put(name, value);
        writeCache(target, name);
    }

    @SuppressWarnings("unchecked")
    private void writeCache(JSONObject target, String name) {
        JSONParser jsonParser = new JSONParser();

        try {
            FileReader reader = new FileReader(plugin.getDataFolder() + "/cache/" + name + ".json");
            JSONArray players = (JSONArray) jsonParser.parse(reader);

            List<JSONObject> list = new ArrayList<>();
            for (Object player : players) {
                JSONObject player_json = (JSONObject) player;
                if (!player_json.get("uuid").equals(target.get("uuid"))) {
                    list.add(player_json);
                }
            }

            for (int i = 0; i < list.size(); i++) {
                if (Integer.parseInt(target.get(name).toString()) > Integer.parseInt(list.get(i).get(name).toString())) {
                    JSONObject temp = list.get(i);
                    list.set(i, target);
                    target = temp;
                }
            }

            list.add(target);

            JSONArray sortedPlayers = new JSONArray();
            sortedPlayers.addAll(list);

            FileWriter writer = new FileWriter(plugin.getDataFolder() + "/cache/" + name + ".json");
            writer.write(sortedPlayers.toJSONString());
            writer.flush();
            writer.close();
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public Long getCache(String file, String name) {
        JSONParser jsonParser = new JSONParser();

        try {
            FileReader reader = new FileReader(plugin.getDataFolder() + "/cache/" + file + ".json");
            JSONArray players = (JSONArray) jsonParser.parse(reader);

            for (Object o : players) {
                JSONObject player = (JSONObject) o;
                if (player.get("lastName").equals(name) || player.get("uuid").equals(name)) {
                    return Long.parseLong(player.get(file).toString());
                }
            }
        } catch (IOException | ParseException e) {
            plugin.getLogger().severe(e.getMessage());
        }

        return null;
    }
}
