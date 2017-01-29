package at.happynev.mwoscoreboardhelper.api;

import at.happynev.mwoscoreboardhelper.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            byte[] apiData = httpGet(new URL(FULL_MECH_URL));
            String dataString = new String(apiData, StandardCharsets.UTF_8);
            JsonObject root = new JsonParser().parse(dataString).getAsJsonObject();
            root.entrySet().forEach(e -> ret.add(gson.fromJson(e.getValue(), Mech.class)));
        } catch (Exception e) {
            Logger.error(e);
            return new ArrayList<>();
        }
        return ret;
    }

    public static byte[] httpGet(URL url) {
        try {
            InputStream is = url.openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            streamCopy(is, bos, 8096);
            bos.close();
            is.close();
            return bos.toByteArray();
        } catch (IOException e) {
            Logger.error(e);
        }
        return null;
    }

    public static long streamCopy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
