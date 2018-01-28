package at.happynev.mwoscoreboardhelper.smurfyapi;

import at.happynev.mwoscoreboardhelper.Logger;
import at.happynev.mwoscoreboardhelper.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nev on 22.01.2017.
 */
public class ApiCaller {
    private static final String FULL_MECH_URL = "http://mwo.smurfy-net.de/api/data/mechs.json";

    public static List<Mech> getAllMechs() {
        List<Mech> ret = new ArrayList<>();
        try {
            Gson gson = new GsonBuilder().setLenient().create();
            byte[] apiData = Utils.httpGet(new URL(FULL_MECH_URL));
            String dataString = new String(apiData, StandardCharsets.UTF_8);
            JsonObject root = new JsonParser().parse(dataString).getAsJsonObject();
            root.entrySet().forEach(e -> ret.add(gson.fromJson(e.getValue(), Mech.class)));
        } catch (Exception e) {
            Logger.error(e);
            return new ArrayList<>();
        }
        return ret;
    }
}
