package xyz.victorolaitan.scholar.session;

import android.content.Context;

import java.util.UUID;

import xyz.victorolaitan.scholar.model.Calendar;
import xyz.victorolaitan.scholar.model.Student;

public final class Session {

    private static Session baseSession;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean newSession(Context context) {
        baseSession = new Session();
        baseSession.database = new LocalStore(context);
        baseSession.security = new LocalSecurity(context) {
            @Override
            DatabaseLink getDatabaseLink() {
                return baseSession.getDatabase();
            }

            @Override
            protected boolean onAuthenticate(UUID id, UUID loginKey) {
                Settings.STUDENT_LOGIN_KEY.set(loginKey);
                baseSession.student = baseSession.database.getStudent(id);
                return baseSession.student != null;
            }
        };
        Settings.init(context);

        return Settings.STUDENT_LOGIN_KEY.get() != null
                && baseSession.security.validateKey(Settings.STUDENT_LOGIN_KEY.get());
    }

    public static Session getSession() {
        return baseSession;
    }

    private Student student;
    private DatabaseLink database;
    private SecurityLink security;

    public Student getStudent() {
        return student;
    }

    public Calendar getCalendar() {
        return student.getCalendar();
    }

    public DatabaseLink getDatabase() {
        return database;
    }

    public SecurityLink.SecureAuthenticator getSecureAuthenticator() {
        return security.getSecureAuthenticator();
    }
}
