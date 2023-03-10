package com.example.demodatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demodatabase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private Button btn_SignUp;
    private TextView tv_SignIn;
    private EditText edt_Email;
    private EditText edt_Password;
    private EditText edt_ConfirmPassword;
    private EditText edt_Username;
    private ProgressDialog progressDialog;
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    private FirebaseFirestore database;
    public static boolean isValid(final String password) {
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();
        init();
        bindingAction();
    }

    private void init() {
        btn_SignUp = findViewById(R.id.btn_signUp);
        tv_SignIn = findViewById(R.id.tv_toLogIn);
        edt_Email = findViewById(R.id.edt_gmail);
        edt_Password = findViewById(R.id.edt_password);
        edt_ConfirmPassword = findViewById(R.id.edt_rePassword);
        edt_Username = findViewById(R.id.edt_username);
        progressDialog = new ProgressDialog(this);
        database = FirebaseFirestore.getInstance();
    }

    private void bindingAction() {

        btn_SignUp.setOnClickListener(view -> onClickSignUp());
        tv_SignIn.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void onClickSignUp() {
        String strEmail = edt_Email.getText().toString().trim();
        boolean validEmail = true,
                validPassword = true,
                validConfirmPassword = true,
                validNickname = true;
        String strPassword = edt_Password.getText().toString().trim();
        String strConfirmPassword = edt_ConfirmPassword.getText().toString().trim();
        if(!strPassword.equals(strConfirmPassword)){
            edt_ConfirmPassword.setError("Password is not matching");
            validConfirmPassword = false;
        }
        if(strEmail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()){
            edt_Email.setError("Please enter valid email");
            validEmail = false;
        }
        if(edt_Password.length() < 8 ){
            edt_Password.setError("Please enter valid password (contains at least 8 characters");
            validPassword = false;
        }
        String nickName = edt_Username.getText().toString().trim();
        if(nickName.isEmpty() ){
            edt_Username.setError("Please enter valid user name");
            validNickname = false;
        }
        if (!validEmail || !validNickname || !validPassword || !validConfirmPassword)
            return;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressDialog.show();
        auth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseUser user = auth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(edt_Username.getText().toString().trim()).build();
                                User userInFirestore = new User();
                                userInFirestore.setDisplayName(edt_Username.getText().toString().trim());
                                userInFirestore.setPassword(strPassword);
                                userInFirestore.setGoogleAccount(false);
                                userInFirestore.setEmail(strEmail);

                                database.collection("users").document(strEmail).set(user);

                                if (user != null) {
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Intent intent = new Intent(SignUpActivity.this, CheckMailActivity.class);
                                                    startActivity(intent);
                                                }
                                            });
                                }
                            }
                        });

                    } else {
                        FancyToast.makeText(this, "Duplicate gmail", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
                        progressDialog.dismiss();

                    }
                });

    }
}