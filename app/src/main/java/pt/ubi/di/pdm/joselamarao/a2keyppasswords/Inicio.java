package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Inicio extends Activity {

    private EditText username,password;
    private ImageView iconKey;
    private AjudanteParaAbrirBD oAPABD;
    private SQLiteDatabase oSQLiteDB;
    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        //para por a imagem na activity Inicio
        iconKey = (ImageView) findViewById(R.id.keyimage);
        iconKey.setImageResource(R.drawable.keyicon);

        oAPABD = new AjudanteParaAbrirBD(this);
        oSQLiteDB = oAPABD.getWritableDatabase();
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

    public void login(View v){
        username= (EditText) findViewById(R.id.username);
        password= (EditText) findViewById(R.id.password);

        String user=username.getText().toString();
        String pass=password.getText().toString();


        if(!user.isEmpty()){
            if(oAPABD.checkUsername(user)){
                if(!pass.isEmpty()){
                    byte[] expectedhash=oAPABD.hashedPassword(user);
                    byte[] salt=oAPABD.salt(user);
                    char[]passarray=pass.toCharArray();

                    if (oAPABD.isExpectedPassword(passarray,salt,expectedhash)){
                            Intent iActivity = new Intent(this, App.class);
                            iActivity.putExtra("username",user);
                            startActivity(iActivity);
                            finish();
                        }else {
                            Toast.makeText(Inicio.this,"Insert a valid password",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(Inicio.this,"Insert a valid password",Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(Inicio.this,"Insert a valid username",Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(Inicio.this,"Insert a valid username",Toast.LENGTH_LONG).show();
            }
    }


    public void registerActivity(View v) {
        finish();
        Intent iActivity = new Intent(this, Register.class);
        startActivity(iActivity);
    }

}

