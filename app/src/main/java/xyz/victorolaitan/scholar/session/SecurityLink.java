package xyz.victorolaitan.scholar.session;

import java.util.UUID;

public abstract class SecurityLink {

    protected abstract boolean onAuthenticate(UUID id);

    protected abstract SecureAuthenticator getSecureAuthenticator();

    protected abstract boolean validateLogin(String email, String password);

    public interface SecureAuthenticator {

        boolean authenticate(String email, String password);
    }
}
