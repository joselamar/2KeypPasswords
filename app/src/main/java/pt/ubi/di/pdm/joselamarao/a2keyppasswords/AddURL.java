package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AddURL extends Activity {
    private SQLiteDatabase oSQLiteDB;
    private AjudanteParaAbrirBD oAPABD;
    private ImageView iconKey;
    private Toolbar toolbar;
    private EditText passwordURL, date, potentialUrl, title, userORemail, description, data;
    private ImageButton showPass;
    private static int iRequest_code = 1;
    private static int icount = 2;
    private static int geticount() {
        icount++;
        return icount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_url);

        //to get Intent
        Intent iCameFromActivity1 = getIntent();
        final String intentusername=iCameFromActivity1.getStringExtra("username");

        //imagem de fundo
        iconKey = (ImageView) findViewById(R.id.keyimage);
        iconKey.setImageResource(R.drawable.keyicon);

        //backbutton
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.backbutton);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iActivity = new Intent(getApplicationContext(),App.class);
                iActivity.putExtra("username",intentusername);
                startActivity(iActivity);
                finish();
            }
        });
        date = (EditText) findViewById(R.id.date);
        date.setClickable(false);
        date.setFocusable(false);

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

    public void setShowPass(View v) {
        int iaux = geticount();
        //Button show pass
        showPass = (ImageButton) findViewById(R.id.showPass);
        passwordURL = (EditText) findViewById(R.id.passwordurl);
        if (iaux % 2 == 0) {
            passwordURL.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else passwordURL.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        //Log.v("TAG",String.valueOf(iaux));
    }

    //validating url as seen in https://stackoverflow.com/questions/5617749/how-to-validate-a-url-website-name-in-edittext-in-android
    public void addURL(View v) {
        potentialUrl = (EditText) findViewById(R.id.url);
        title = (EditText) findViewById(R.id.entry);
        String titlea = title.getText().toString();
        userORemail = (EditText) findViewById(R.id.usernameORemail);
        String userORemaila = userORemail.getText().toString();
        passwordURL = (EditText) findViewById(R.id.passwordurl);
        String pass = passwordURL.getText().toString().toLowerCase();
        description = (EditText) findViewById(R.id.description);
        String description1 = description.getText().toString();
        data = (EditText) findViewById(R.id.date);
        String expirationdate = data.getText().toString();
        String url = potentialUrl.getText().toString().toLowerCase();
        Intent iCameFromActivity1 = getIntent();
        final String intentusername=iCameFromActivity1.getStringExtra("username");

        byte[]passARRAY=pass.getBytes();
        byte[] hash=oAPABD.hashedPassword(intentusername);
        SecretKeySpec key=oAPABD.getKey(hash);
        IvParameterSpec iv=oAPABD.getIvSpec();
        byte[]ivaux=iv.getIV();


        if (validateInfo()) {
            ContentValues oCV = new ContentValues();
            oCV.put(oAPABD.COLF, intentusername);
            oCV.put(oAPABD.COL11, titlea);
            oCV.put(oAPABD.COL22, url);
            oCV.put(oAPABD.COL33, userORemaila);
            try {
                byte[]encrypted = oAPABD.encrypt(passARRAY,hash,iv,key);
                if(encrypted==null)
                    Log.v("TAG", "didn't encrypt");
                oCV.put(oAPABD.COL44, encrypted);
            } catch (Exception e){
                Log.e("TAG", "Error");
            }
            oCV.put(oAPABD.COL55, description1);
            oCV.put(oAPABD.COL66, expirationdate);
            oCV.put(oAPABD.COL77, ivaux);
            oSQLiteDB.insert(oAPABD.TABLE_NAME1, null, oCV);
            finish();
            Intent iActivity = new Intent(this, App.class);
            iActivity.putExtra("username", intentusername);
            startActivity(iActivity);
        }
    }

    //https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext user OWADVL
    public void choosedate(View v) {
        final Calendar myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.DATE,0);

        final DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yyyy"; // your format
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                date.setText(sdf.format(myCalendar.getTime()));
            }

        };
        new DatePickerDialog(this, date1, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    public void generatePass(View v) {
        Intent iResult = new Intent(this, GeneratePassword.class);
        startActivityForResult(iResult, iRequest_code);
    }

    public void onActivityResult(int iReqCode, int iResultCode, Intent iResult) {
        if ((iReqCode == iRequest_code) && (iResultCode == RESULT_OK)) {
            passwordURL = (EditText) findViewById(R.id.passwordurl);
            passwordURL.setText(iResult.getStringExtra("pass"));
        }
    }

    protected boolean validateInfo() {
        potentialUrl = (EditText) findViewById(R.id.url);
        title = (EditText) findViewById(R.id.entry);
        String titlea = title.getText().toString();
        userORemail = (EditText) findViewById(R.id.usernameORemail);
        String userORemaila = userORemail.getText().toString();
        passwordURL = (EditText) findViewById(R.id.passwordurl);
        String pass = passwordURL.getText().toString().toLowerCase();
        description = (EditText) findViewById(R.id.description);
        String description1 = description.getText().toString();
        data = (EditText) findViewById(R.id.date);
        String expirationdate = data.getText().toString();
        String url = potentialUrl.getText().toString();
        url=url.toLowerCase();
        Intent iCameFromActivity1 = getIntent();
        final String intentusername=iCameFromActivity1.getStringExtra("username");

        if (titlea.isEmpty()) {
            Toast.makeText(AddURL.this, "Insert a valid title", Toast.LENGTH_LONG).show();
            return false;
        }
        if(oAPABD.isDuplicatedTitle(intentusername,titlea) || oAPABD.isDuplicatedTitle(intentusername,titlea.toLowerCase())){
            Toast.makeText(AddURL.this, "That title already exists", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!Patterns.WEB_URL.matcher(url).matches() || url.isEmpty()) {
            Toast.makeText(AddURL.this, "Insert a valid url", Toast.LENGTH_LONG).show();
            return false;
        }
        if (oAPABD.checkURL(url, intentusername)) {
            Toast.makeText(AddURL.this, "That url already exists", Toast.LENGTH_LONG).show();
            return false;
        }
        if (userORemaila.isEmpty()) {
            Toast.makeText(AddURL.this, "Insert a valid email or username", Toast.LENGTH_LONG).show();
            return false;
        }
        if(pass.isEmpty()){
            Toast.makeText(AddURL.this, "Insert a valid password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (description1.isEmpty()) {
            Toast.makeText(AddURL.this, "Insert a valid description", Toast.LENGTH_LONG).show();
            return false;
        }
        if (expirationdate.isEmpty()) {
            Toast.makeText(AddURL.this, "Insert a valid Expiration date", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}