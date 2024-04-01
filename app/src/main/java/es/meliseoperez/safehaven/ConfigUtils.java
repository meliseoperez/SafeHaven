package es.meliseoperez.safehaven;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {
    private static final String CONFIG_FILE_NAME = "config.properties";
    public static String getServerIp(Context context){
        Properties properties = new Properties();
        try{
            InputStream inputStream= context.getAssets().open(CONFIG_FILE_NAME);
            properties.load(inputStream);
            return properties.getProperty("server_ip");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
