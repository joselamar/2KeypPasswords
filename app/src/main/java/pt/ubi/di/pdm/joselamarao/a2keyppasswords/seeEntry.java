package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static pt.ubi.di.pdm.joselamarao.a2keyppasswords.AjudanteParaAbrirBD.TABLE_NAME1;

public class seeEntry extends Activity {
    private SQLiteDatabase oSQLiteDB;
    private AjudanteParaAbrirBD oAPABD;
    private ImageView iconKey;
    private Toolbar toolbar;
    private ImageButton showPass,showCalendar;
    private EditText passwordURL, date, potentialUrl, title, userORemail, description, data;
    private static int iRequest_code = 1;
    private static int icount=2;
    private static int geticount() {
        icount++;
        return icount;
    }
    private TextView geraPass;
    private Button update;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_entry);

        Intent iCameFromActivity1 = getIntent();
        final String intentusername= iCameFromActivity1.getStringExtra("username");
        final String intenttitle=iCameFromActivity1.getStringExtra("title");

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

        setDecryptedPass(intenttitle);
        setOtherContent(intenttitle);
        String dataa=date.getText().toString();

        try {
            if (!isExpirationDateValid(dataa)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.seeEntry);
                builder.setTitle("Date expired")
                        .setMessage("Do you want to generate a new password")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                generatePass(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                choosedateEntry(null);
                            }
                        });
                builder.show();
            }
        } catch (Exception e){
            Log.e("TAG", e.toString());
        }

        showPass=(ImageButton)findViewById(R.id.showPass);
        showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int iaux = geticount();
                //Button show pass
                showPass = (ImageButton) findViewById(R.id.showPass);
                passwordURL = (EditText) findViewById(R.id.passwordurl);
                if (iaux % 2 == 0) {
                    passwordURL.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else passwordURL.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                //Log.v("TAG",String.valueOf(iaux));
            }
        });

        geraPass=(TextView)findViewById(R.id.passwordgenerator);
        geraPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iResult = new Intent(getApplicationContext(), GeneratePassword.class);
                startActivityForResult(iResult, iRequest_code);
            }
        });

        showCalendar=(ImageButton)findViewById(R.id.choosedateb);
        showCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        Log.v("TAG", "wtf");
                    }

                };
                Log.v("TAG", "WTF2");
                new DatePickerDialog(seeEntry.this, date1, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        update=(Button)findViewById(R.id.adder);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordURL = (EditText) findViewById(R.id.passwordurl);
                String pass = passwordURL.getText().toString().toLowerCase();
                data = (EditText) findViewById(R.id.date);
                String expirationdate = data.getText().toString();

                Intent iCameFromActivity1 = getIntent();
                final String intentusername=iCameFromActivity1.getStringExtra("username");

                byte[]passARRAY=pass.getBytes();
                byte[] hash=oAPABD.hashedPassword(intentusername);
                SecretKeySpec key=oAPABD.getKey(hash);
                IvParameterSpec iv=oAPABD.getIvSpec();
                byte[]ivaux=iv.getIV();
                if (validateInfo()) {
                    ContentValues oCV = new ContentValues();
                    try {
                        byte[]encrypted = oAPABD.encrypt(passARRAY,hash,iv,key);
                        if(encrypted==null)
                            Log.v("TAG", "didn't encrypt");
                        oCV.put(oAPABD.COL44, encrypted);
                    } catch (Exception e){
                        Log.e("TAG", "Error");
                    }
                    oCV.put(oAPABD.COL66, expirationdate);
                    oCV.put(oAPABD.COL77,ivaux);
                    oSQLiteDB.update(oAPABD.TABLE_NAME1, oCV, "username = ?",new String[]{intentusername});
                    Toast.makeText(seeEntry.this, "Entry updated", Toast.LENGTH_LONG).show();
                    finish();
                    Intent iActivity = new Intent(getApplicationContext(), App.class);
                    iActivity.putExtra("username", intentusername);
                    startActivity(iActivity);
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


    public void choosedateEntry(View v) {
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

    public boolean validateInfo() {
        passwordURL = (EditText) findViewById(R.id.passwordurl);
        String pass = passwordURL.getText().toString().toLowerCase();
        description = (EditText) findViewById(R.id.description);
        String description1 = description.getText().toString();
        data = (EditText) findViewById(R.id.date);
        String expirationdate = data.getText().toString();
        String url = potentialUrl.getText().toString();

        Intent iCameFromActivity1 = getIntent();
        final String intentusername=iCameFromActivity1.getStringExtra("username");

       if(pass.isEmpty()){
           Toast.makeText(seeEntry.this, "Insert a valid Password", Toast.LENGTH_LONG).show();
            return false;
       }
        if (expirationdate.isEmpty()) {
            Toast.makeText(seeEntry.this, "Insert a valid Expiration date", Toast.LENGTH_LONG).show();
            return false;
        }
        try{
            if(!isExpirationDateValid(expirationdate)) {
                Toast.makeText(seeEntry.this, "Insert a valid Expiration date", Toast.LENGTH_LONG).show();
                return false;
            }
        }catch (Exception e){
            Log.v("TAG", "FATAL ERROR SYSTEM IS CLOSING NOW");
        }
        return true;
    }

    public void setDecryptedPass (String titulo){
        passwordURL=(EditText)findViewById(R.id.passwordurl);

        Intent iCameFromActivity1 = getIntent();
        final String intentusername= iCameFromActivity1.getStringExtra("username");
        Cursor oCursor = oSQLiteDB.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ? and title= ?" , new String[]{intentusername, titulo});
        if(oCursor.moveToFirst()){
            byte[] encripted = oAPABD.getEncryptedPass(intentusername,titulo);
            byte[] iv = oAPABD.getIv(intentusername,titulo);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            byte[] hash=oAPABD.hashedPassword(intentusername);
            SecretKeySpec key=oAPABD.getKey(hash);
            try {
                byte[] decrypt=oAPABD.decrypt(encripted,ivspec,key);
                String decript = new String(decrypt);
                passwordURL.setText(decript);
            } catch (Exception e) {
                Log.e("TAG", "Error decrypting");
            }
        }
    }

    public void setOtherContent(String titulo){
        Intent iCameFromActivity1 = getIntent();
        final String intentusername= iCameFromActivity1.getStringExtra("username");
        Cursor oCursor = oSQLiteDB.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ? and title= ?" , new String[]{intentusername, titulo});
        if(oCursor.moveToFirst()){
            String title1=oCursor.getString(2);
            String url=oCursor.getString(3);
            String user=oCursor.getString(4);
            String description1=oCursor.getString(6);
            String expirationdate=oCursor.getString(7);
            potentialUrl = (EditText) findViewById(R.id.url);
            potentialUrl.setText(url);
            title =(EditText)findViewById(R.id.entry);
            title.setText(title1);
            userORemail = (EditText) findViewById(R.id.usernameORemail);
            userORemail.setText(user);
            description = (EditText) findViewById(R.id.description);
            description.setText(description1);
            data = (EditText) findViewById(R.id.date);
            data.setText(expirationdate);
        }
    }

    protected boolean isExpirationDateValid(String date) throws ParseException {
        Date todaydate = new SimpleDateFormat("dd/MM/yyyy").parse(date);
        return new Date().before(todaydate);
    }

    //for invalid date in junction with the onClickListener
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

    public void updateEntry (View v){
    passwordURL = (EditText) findViewById(R.id.passwordurl);
    String pass = passwordURL.getText().toString().toLowerCase();
    data = (EditText) findViewById(R.id.date);
    String expirationdate = data.getText().toString();

    Intent iCameFromActivity1 = getIntent();
    final String intentusername=iCameFromActivity1.getStringExtra("username");

    byte[]passARRAY=pass.getBytes();
    byte[] hash=oAPABD.hashedPassword(intentusername);
    SecretKeySpec key=oAPABD.getKey(hash);
    IvParameterSpec iv=oAPABD.getIvSpec();
    byte[]ivaux=iv.getIV();
        if (validateInfo()) {
        ContentValues oCV = new ContentValues();
        try {
            byte[]encrypted = oAPABD.encrypt(passARRAY,hash,iv,key);
            if(encrypted==null)
                Log.v("TAG", "didn't encrypt");
            oCV.put(oAPABD.COL44, encrypted);
        } catch (Exception e){
            Log.e("TAG", "Error");
        }
        oCV.put(oAPABD.COL66, expirationdate);
        oCV.put(oAPABD.COL77,ivaux);
        oSQLiteDB.update(oAPABD.TABLE_NAME1, oCV, "username = ?",new String[]{intentusername});
        Toast.makeText(seeEntry.this, "Entry updated", Toast.LENGTH_LONG).show();
        finish();
        Intent iActivity = new Intent(this, App.class);
        iActivity.putExtra("username", intentusername);
        startActivity(iActivity);
        }
    }
}
