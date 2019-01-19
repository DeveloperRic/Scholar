package xyz.victorolaitan.scholar.session;

import android.content.Context;

import java.io.File;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;

final class Settings {
    private static EasyJSON settings;
    static UUID localStudentId;

    static void init(Context context) {
        File file = new File(context.getFilesDir(), "settings.txt");
        if (file.exists()) {
            try {
                settings = EasyJSON.open(file);
                localStudentId = UUID.fromString(settings.valueOf("localStudentId"));
            } catch (EasyJSONException e) {
                e.printStackTrace();
            }
        }
    }

    static boolean save() {
        if (settings == null)
            return false;
        try {
            settings.save();
            return true;
        } catch (EasyJSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
