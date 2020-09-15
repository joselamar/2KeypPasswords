package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.Random;

public class GeneratePassword extends Activity {
    private EditText pass,length,choosedate;
    private SeekBar seekbar;
    private CheckBox uppercase1,lowercase1,digits1,special1,operationsign1,underline1,brackets1,others1,all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_password);

        SeekBar seekBar = findViewById(R.id.seekbar);
        pass=(EditText) findViewById(R.id.generatedPass);
        length=(EditText)findViewById(R.id.passLength);

        //to give a password wiht selected characters
        uppercase1=(CheckBox)findViewById(R.id.uppercase);
        lowercase1=(CheckBox)findViewById(R.id.lowercase);
        digits1=(CheckBox)findViewById(R.id.digits);
        uppercase1.setChecked(true);
        lowercase1.setChecked(true);
        digits1.setChecked(true);

        //pop up activity adapted as seen in https://www.youtube.com/watch?v=eX-TdY6bLdg
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width= dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8),(int)(height*0.8));
        WindowManager.LayoutParams params= getWindow().getAttributes();
        params.gravity= Gravity.CENTER;
        params.x = 0;
        params.y = 0;
        getWindow().setAttributes(params);

        seekBar.setOnSeekBarChangeListener(seekBarChangeListener); //to change pass while sliding seekbar
        String randomPass=geraPassRand(15); //for first case
        pass.setText(randomPass);
        length.setFocusable(false); //to not change length
        length.setClickable(true);
        pass.setClickable(false);
        pass.setFocusable(false);
        pass.setLongClickable(false);


    }

    //adapted as seen in https://stackoverflow.com/questions/40185629/how-to-generate-random-passwords-with-options-in-java
    public String geraPassRand(int length){
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits= "0123456789";
        String special = "~`!@#$%^&*";
        String operationsignals = "-=+";
        String underline = "_";
        String brackets = "()[{]}\\|";
        String others = ";:\\'\\\",<.>/?";
        String returned = "";
        String everyone="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*\"-=+_()[{]}\\|;:\\'\\\",<.>/?";

        uppercase1=(CheckBox)findViewById(R.id.uppercase);
        lowercase1=(CheckBox)findViewById(R.id.lowercase);
        digits1=(CheckBox)findViewById(R.id.digits);
        special1=(CheckBox)findViewById(R.id.special);
        operationsign1=(CheckBox)findViewById(R.id.operationsign);
        underline1=(CheckBox)findViewById(R.id.underline);
        brackets1=(CheckBox)findViewById(R.id.brackets);
        others1=(CheckBox)findViewById(R.id.other);
        all=(CheckBox)findViewById(R.id.all);

        if(all.isChecked()){
            returned=everyone;
            uppercase1.setChecked(false);
            lowercase1.setChecked(false);
            digits1.setChecked(false);
            special1.setChecked(false);
            operationsign1.setChecked(false);
            underline1.setChecked(false);
            brackets1.setChecked(false);
            others1.setChecked(false);

            Random random = new SecureRandom();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(returned.charAt(random.nextInt(returned.length())));
            }

            return sb.toString();
        } else {
            if (uppercase1.isChecked())
                returned += uppercase;
            if (lowercase1.isChecked())
                returned += lowercase;
            if(digits1.isChecked())
                returned +=digits;
            if(special1.isChecked())
                returned +=special;
            if(operationsign1.isChecked())
                returned+=operationsignals;
            if(underline1.isChecked())
                returned+=underline;
            if(brackets1.isChecked())
                returned +=brackets;
            if(others1.isChecked())
                returned += others;
        }

        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(returned.charAt(random.nextInt(returned.length())));
        }

        return sb.toString();
    }

    //adapted as seen in https://stackoverflow.com/questions/8629535/implementing-a-slider-seekbar-in-android
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            seekbar = (SeekBar) findViewById(R.id.seekbar);
            pass = (EditText) findViewById(R.id.generatedPass);
            length = (EditText) findViewById(R.id.passLength);
            length.setFocusable(false);
            length.setClickable(true);
            pass.setTextIsSelectable(true);

            int length1 = seekbar.getProgress();

            length.setText(String.valueOf(length1));

            String password = geraPassRand(length1);
            pass.setText(password);
            pass.setClickable(false);
            pass.setFocusable(false);
            pass.setLongClickable(false);

            }

            @Override
            public void onStartTrackingTouch (SeekBar seekBar){
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch (SeekBar seekBar){
                // called after the user finishes moving the SeekBar
            }
        };

    public void copyPass(View v){
        Intent iResult = new Intent();
        iResult.putExtra("pass",pass.getText().toString());
        setResult(RESULT_OK,iResult);
        finish();
    }
}

