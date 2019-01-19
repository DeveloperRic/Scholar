package xyz.victorolaitan.scholar.session;

import android.content.Context;

import java.util.UUID;

import xyz.victorolaitan.scholar.model.Calendar;
import xyz.victorolaitan.scholar.model.Student;

public final class Session {

    private static Session baseSession;

    public static boolean newSession(Context context) {
        baseSession = new Session();
        baseSession.database = new LocalStore(context);
        baseSession.security = new LocalSecurity(context) {
            @Override
            protected boolean onAuthenticate(UUID id) {
                Settings.localStudentId = id;
                Settings.save();
                baseSession.student = baseSession.database.getStudent(Settings.localStudentId);
                return baseSession.student != null;
            }
        };
        Settings.save();
        Settings.init(context);

        return Settings.localStudentId != null
                && baseSession.security.onAuthenticate(Settings.localStudentId);
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
