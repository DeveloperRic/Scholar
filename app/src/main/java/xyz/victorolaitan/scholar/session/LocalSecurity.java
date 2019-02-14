package xyz.victorolaitan.scholar.session;

import android.content.Context;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import xyz.victorolaitan.easyjson.EasyJSON;
import xyz.victorolaitan.easyjson.EasyJSONException;
import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.easyjson.SafeJSONElementType;
import xyz.victorolaitan.scholar.model.Student;

abstract class LocalSecurity extends SecurityLink {
    private static final String SECURITY_FILE = "security.json";
    private Context context;

    LocalSecurity(Context context) {
        this.context = context;
    }

    @Override
    protected SecureAuthenticator getSecureAuthenticator() {
        return new SecureAuthenticator() {
            @Override
            public boolean newStudent(String email, String password) {
                EasyJSON json = open();
                for (JSONElement obj : json.getRootNode().getChildren()) {
                    if (obj.valueOf("email").equals(email)
                            && obj.valueOf("password").equals(password)) {
                        return false;
                    }
                }
                Student student = new Student("Scholar", "Anon", email, new Date());
                if (getDatabaseLink().postPerson(student)) {
                    JSONElement struct = json.putStructure("");
                    struct.putPrimitive("id", student.getId().toString());
                    struct.putPrimitive("email", email);
                    struct.putPrimitive("password", password);
                    UUID loginKey = UUID.randomUUID();
                    struct.putPrimitive("loginKey", loginKey.toString());
                    try {
                        json.save();
                        return onAuthenticate(student.getId(), loginKey);
                    } catch (EasyJSONException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean authenticate(String email, String password) {
                return validateLogin(email, password);
            }
        };
    }

    @Override
    protected boolean validateLogin(String email, String password) {
        EasyJSON json = open();
        for (JSONElement obj : json.getRootNode().getChildren()) {
            if (obj.valueOf("email").equals(email)
                    && obj.valueOf("password").equals(password)) {
                return onAuthenticate(
                        UUID.fromString(obj.valueOf("id")),
                        UUID.fromString(obj.valueOf("loginKey")));
            }
        }
        return false;
    }

    @Override
    protected boolean validateKey(UUID loginKey) {
        EasyJSON json = open();
        for (JSONElement obj : json.getRootNode().getChildren()) {
            if (obj.valueOf("loginKey").equals(loginKey.toString())) {
                return onAuthenticate(UUID.fromString(obj.valueOf("id")), loginKey);
            }
        }
        return false;
    }

    private EasyJSON open() {
        File file = new File(context.getFilesDir(), SECURITY_FILE);
        if (file.exists()) {
            try {
                return EasyJSON.open(file);
            } catch (EasyJSONException e) {
                e.printStackTrace();
            }
        }
        EasyJSON json = EasyJSON.create(file);
        json.getRootNode().setType(SafeJSONElementType.ARRAY);
        return json;
    }
}
