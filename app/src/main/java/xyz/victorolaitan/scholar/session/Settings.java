package xyz.victorolaitan.scholar.session;

import android.content.Context;

import java.io.File;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;
import xyz.victorolaitan.easyjson.JSONElement;

public enum Settings {

    STUDENT_LOGIN_KEY(new Handler<UUID>() {
        @Override
        String getKey() {
            return "studentLoginKey";
        }

        @Override
        void setToDefault() {
        }

        @Override
        protected void read(JSONElement parent) {
            obj = UUID.fromString(parent.valueOf(getKey()));
        }

        @Override
        protected void write(JSONElement parent) {
            parent.putPrimitive(getKey(), obj.toString());
        }
    });

    private Handler handler;

    Settings(Handler handler) {
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public <T> T get() {
        if (handler.obj == null) {
            handler.readWithDefault(settings.getRootNode());
        }
        return (T) handler.obj;
    }

    public boolean set(Object newValue) {
        handler.obj = newValue;
        handler.write(settings.getRootNode());
        try {
            settings.save();
            return true;
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static EasyJSON settings;

    static void init(Context context) {
        File file = new File(context.getFilesDir(), "settings.json");
        if (file.exists()) {
            try {
                settings = EasyJSON.open(file);
            } catch (EasyJSONException e) {
                e.printStackTrace();
            }
            for (Settings setting : values()) {
                try {
                    setting.handler.readWithDefault(settings.getRootNode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            settings = EasyJSON.create(file);
        }
    }

    private static abstract class Handler<T> {
        T obj;

        abstract String getKey();

        private void readWithDefault(JSONElement parent) {
            if (parent.elementExists(getKey())) {
                read(parent);
            } else {
                setToDefault();
            }
        }

        abstract void setToDefault();

        protected abstract void read(JSONElement parent);

        protected abstract void write(JSONElement parent);
    }

    private static abstract class DefaultHandler<T> extends Handler<T> {

        @Override
        protected void read(JSONElement parent) {
            obj = parent.valueOf(getKey());
        }

        @Override
        protected void write(JSONElement parent) {
            parent.putPrimitive(getKey(), obj);
        }
    }
}
