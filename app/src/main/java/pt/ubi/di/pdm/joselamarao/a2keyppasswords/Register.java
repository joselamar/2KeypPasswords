package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;



public class Register extends Activity {
    private SQLiteDatabase oSQLiteDB;
    private AjudanteParaAbrirBD oAPABD;
    private EditText mail,username,password,confirmpassword;
    private ImageView iconKey;
    private Toolbar toolbar;
    private ProgressDialog progress;
    private Button register;

    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$"; // a to z numbers from 0 to 9 _ and hifen, lenght from 3 to 15

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //imagem de fundo
        iconKey = (ImageView) findViewById(R.id.keyimage);
        iconKey.setImageResource(R.drawable.keyicon);

        //base de dados
        oAPABD = new AjudanteParaAbrirBD(this);
        oSQLiteDB = oAPABD.getWritableDatabase();


        //backbutton
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.backbutton);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(),Inicio.class));
            }
        });

        register=(Button)findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mail= (EditText) findViewById(R.id.mail);
                username= (EditText) findViewById(R.id.username);
                password= (EditText) findViewById(R.id.password);
                confirmpassword=(EditText) findViewById(R.id.confirmpassword);

                String email= mail.getText().toString();
                String user=username.getText().toString();
                String pass=password.getText().toString();


                if(validateINFO()){
                    char[] passarray =pass.toCharArray();
                    byte[] saltkey = oAPABD.getNextSalt();
                    byte[] hashed=oAPABD.hash(passarray,saltkey);
                    //String test= Base64.encodeToString(hashed, Base64.DEFAULT);
                    //String salt= Base64.encodeToString(saltkey, Base64.DEFAULT);

                    ContentValues oCV = new ContentValues();
                    oCV.put(oAPABD.COL1,email);
                    oCV.put(oAPABD.COL2,user);
                    oCV.put(oAPABD.COL3, hashed);
                    oCV.put(oAPABD.COL4, saltkey);
                    oSQLiteDB.insert(oAPABD.TABLE_NAME, null ,oCV);
                    Toast.makeText(Register.this,"You have been registered",Toast.LENGTH_LONG).show();

                    Intent iActivity = new Intent(getApplicationContext(), Inicio.class);
                    startActivity(iActivity);
                    finish();
                } else {
                    Log.v("TAG","Error validating info or adding to db");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        oSQLiteDB = oAPABD.getWritableDatabase();
    }

    @Override
    protected void onPause() {
        super.onPause();
        oAPABD.close();
    }

    protected boolean validateINFO(){
        mail= (EditText) findViewById(R.id.mail);
        username= (EditText) findViewById(R.id.username);
        password= (EditText) findViewById(R.id.password);
        confirmpassword=(EditText) findViewById(R.id.confirmpassword);

        String email= mail.getText().toString();
        String user=username.getText().toString();
        String pass=password.getText().toString();
        String confPass=confirmpassword.getText().toString();

        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Pattern userPattern = Pattern.compile(USERNAME_PATTERN);

        //to check if email is valid used function given by android
        if(!emailPattern.matcher(email).matches()){
            Toast.makeText(Register.this,"Insert a valid Email",Toast.LENGTH_LONG).show();
            mail.selectAll();
            return false;
        }

        //check if email is on db!
        if(oAPABD.checkEmail(email)){
            Toast.makeText(Register.this,"E-mail already registered",Toast.LENGTH_LONG).show();
            return false;
        }

        //validate username
        if(!userPattern.matcher(user).matches()){
            Toast.makeText(Register.this,"Insert a valid Username between 3 and 15 caracters with lowercase",Toast.LENGTH_LONG).show();
            username.selectAll();
            return false;
        }

        //check if user is on db
        if(oAPABD.checkUsername(user)){
            Toast.makeText(Register.this,"Username already in use",Toast.LENGTH_LONG).show();
            return false;
        }
        //validate password
        //validate password
        if(pass.isEmpty()){
            Toast.makeText(Register.this,"Insert a valid password between 4 and 50 characters",Toast.LENGTH_LONG).show();
            return false;
        }
        if(!pass.equals(confPass)){
            Toast.makeText(Register.this,"Passwords don't match",Toast.LENGTH_LONG).show();
            return false;
        }
        if(pass.length()>50 || pass.length()<4){
            Toast.makeText(Register.this,"Insert a valid password between 4 and 50 characters",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


}
