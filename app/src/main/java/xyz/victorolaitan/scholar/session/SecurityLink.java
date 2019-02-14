package xyz.victorolaitan.scholar.session;

import java.util.UUID;

public abstract class SecurityLink {

    abstract DatabaseLink getDatabaseLink();

    protected abstract boolean onAuthenticate(UUID id, UUID loginKey);

    protected abstract SecureAuthenticator getSecureAuthenticator();

    protected abstract boolean validateLogin(String email, String password);

    protected abstract boolean validateKey(UUID loginKey);

    public interface SecureAuthenticator {

        boolean newStudent(String email, String password);

        boolean authenticate(String email, String password);
    }
}
