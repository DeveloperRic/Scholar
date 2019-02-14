package xyz.victorolaitan.scholar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import xyz.victorolaitan.scholar.session.Session;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText editEmail = findViewById(R.id.login_editEmail);
        EditText editPass = findViewById(R.id.login_editPass);
        EditText editPassConfirm = findViewById(R.id.login_editPassConfirm);

        findViewById(R.id.login_btnCreateAccount).setOnClickListener(v -> {
            if (Session.getSession().getSecureAuthenticator()
                    .newStudent(editEmail.getText().toString(), editPass.getText().toString())) {
                startActivity(new Intent(this, MainActivity.class));
            }
        });
        findViewById(R.id.login_btnLogin).setOnClickListener(v -> {
            if (Session.getSession().getSecureAuthenticator()
                    .authenticate(editEmail.getText().toString(), editPass.getText().toString())) {
                startActivity(new Intent(this, MainActivity.class));
            }
        });
    }
}
