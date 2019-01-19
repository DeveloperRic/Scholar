package xyz.victorolaitan.scholar.session;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;
import xyz.victorolaitan.easyjson.JSONElement;

abstract class LocalSecurity extends SecurityLink {
    private static final String SECURITY_FILE = "security.txt";
    private Context context;

    LocalSecurity(Context context) {
        this.context = context;
    }

    @Override
    protected SecureAuthenticator getSecureAuthenticator() {
        return this::validateLogin;
    }

    @Override
    protected boolean validateLogin(String email, String password) {
        File file = new File(context.getFilesDir(), SECURITY_FILE);
        if (file.exists()) {
            try {
                EasyJSON json = EasyJSON.open(file);
                for (JSONElement obj : json.getRootNode().getChildren()) {
                    if (obj.valueOf("e").equals(email)
                            && obj.valueOf("p").equals(password)) {
                        return true;
                    }
                }
            } catch (EasyJSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
