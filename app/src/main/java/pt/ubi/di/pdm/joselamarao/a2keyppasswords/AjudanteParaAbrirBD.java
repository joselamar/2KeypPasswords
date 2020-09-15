package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Blob;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AjudanteParaAbrirBD extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "usermanager" ;
    protected static final String TABLE_NAME ="user";
    protected static final String COL1 = "email";
    protected static final String COL2 = "username";
    protected static final String COL3 = "password";
    protected static final String COL4 = "salt";
    protected static final String TABLE_NAME1 ="entry";
    protected static final String COLID = "id";
    protected static final String COLF = "username";
    protected static final String COL11 = "title";
    protected static final String COL22 = "url";
    protected static final String COL33 = "userORemail";
    protected static final String COL44 = "password";
    protected static final String COL55 = "description";
    protected static final String COL66 = "expirationdate";
    protected static final String COL77 = "iv";



    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static final String CREATE_USER = "CREATE TABLE " + TABLE_NAME + "(" + COL1 + " VARCHAR(30) PRIMARY KEY ," + COL2 + " VARCHAR(20) ," + COL3 + " BLOB," + COL4 + " BLOB" + ")";
    private static final String CREATE_ENTRY = "CREATE TABLE " + TABLE_NAME1 + "("+ COLID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLF + " VARCHAR(30) REFERENCES "+TABLE_NAME+ "(" + "COL2" + "),"
            + COL11 + " VARCHAR(30) ," + COL22 + " VARCHAR(50) ," + COL33 + " VARCHAR(30) ," + COL44 + " BLOB ,"
            + COL55 + " VARCHAR(50) ," + COL66 + " VARCHAR(10) ," + COL77+ " BLOB" + ")";

    public AjudanteParaAbrirBD ( Context context ) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_ENTRY);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion , int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_NAME + ";");
        db.execSQL(CREATE_USER);
    }

    //adaptado do video https://www.youtube.com/watch?v=3RewvdB82PY
    public boolean checkEmail(String email){
        String [] columns = { COL1 };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL1 +" = ? ";
        String[] selectionArgs = { email };

        Cursor cursor = db.query("user",columns,selection,selectionArgs,null,null,null);
        int cursorCount = cursor.getCount();
        cursor.close();

        if (cursorCount>0)
            return true;
        return false;
    }

    public boolean checkUsername(String username){
        String [] columns = { COL1 };
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL2 +" = ?";
        String[] selectionArgs = { username };

        Cursor cursor = db.query("user",columns,selection,selectionArgs,null,null,null);

        int cursorCount = cursor.getCount();
        cursor.close();

        if (cursorCount>0)
            return true;
        return false;
    }

    public byte[] hashedPassword(String username){
        byte[] hashedBLOB=null;
        String[] columns = {COL3};
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL2 + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query("user",columns,selection,selectionArgs,null,null,null);
        if(cursor.moveToFirst()){
           hashedBLOB=cursor.getBlob(0);
        }
        if(cursor!=null){
            return hashedBLOB;
        }
        else return null;
    }

    public byte[] salt(String username){
        byte[] salted=null;
        String[] columns = {COL4};
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL2 + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query("user",columns,selection,selectionArgs,null,null,null);
        if(cursor.moveToFirst()){
            salted=cursor.getBlob(0);
        }

        if(cursor!=null){
            return salted;
        }
        else return null;
    }

    public byte[] getIv(String username,String titulo){
        byte[] ivy=null;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor oCursor = db.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ? and title= ?" , new String[]{username, titulo});
        if(oCursor.moveToFirst()){
            ivy=oCursor.getBlob(8);
        }

        if(oCursor!=null){
            return ivy;
        }
        else return null;
    }

    public byte[] getEncryptedPass(String username,String titulo){
        byte[] encrypted=null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor oCursor = db.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ? and title= ?" , new String[]{username, titulo});
        if(oCursor.moveToFirst()){
            encrypted=oCursor.getBlob(5);
        }

        if(oCursor!=null){
            return encrypted;
        }
        else return null;
    }

    public boolean checkURL(String url,String username){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL22 +" = ? " + " AND " + COLF + " =?";
        String[] selectionArgs = { url, username };

        Cursor cursor = db.query("entry",null,selection,selectionArgs,null,null,null);
        int cursorCount = cursor.getCount();
        cursor.close();

        if (cursorCount>0)
            return true;
        return false;
    }

    //adapataded from https://stackoverflow.com/questions/18142745/how-do-i-generate-a-salt-in-java-for-salted-hash user assylias
    public static byte[] getNextSalt() {
        byte[] salt = new byte[256];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean isExpectedPassword(char[] password, byte[] salt, byte[] expectedHash) {
        byte[] pwdHash = hash(password, salt);
        Arrays.fill(password, Character.MIN_VALUE);
        if (pwdHash.length != expectedHash.length) return false;
        for (int i = 0; i < pwdHash.length; i++) {
            if (pwdHash[i] != expectedHash[i]) return false;
        }
        return true;
    }

    //adaptaded from https://code.tutsplus.com/tutorials/storing-data-securely-on-android--cms-30558
    public SecretKeySpec getKey(byte[]hash){
        SecretKeySpec keySpec = new SecretKeySpec(hash, "AES");
        return keySpec;
    }

    public IvParameterSpec getIvSpec() {
        SecureRandom ivRandom = new SecureRandom(); //not caching previous seeded instance of SecureRandom
        byte[] iv = new byte[16];
        ivRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        return ivSpec;
    }

    public byte[] encrypt (byte[] message, byte [] hash, IvParameterSpec iv, SecretKeySpec key) throws Exception{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encrypted = cipher.doFinal(message);
            return encrypted;
    }

    public byte[] decrypt (byte[] encrypted, IvParameterSpec iv ,SecretKeySpec key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[]decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public boolean isDuplicatedTitle(String username,String titulo){
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor oCursor = db.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ? and title= ?" , new String[]{username, titulo});
            if(oCursor.getCount()>0){
                return true;
            }
            return false;
        }
}
     /*public boolean checkPassword (String password, String username) {
        String[] columns = {COL1};
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL2 + " = ?" + " AND " + COL3 + " =?";
        String[] selectionArgs = {username, password};


        Cursor cursor = db.query("user", columns, selection, selectionArgs, null, null, null);

        int cursorCount = cursor.getCount();
        cursor.close();

        if (cursorCount>0)
            return true;
        return false;
    } */
/*    public String getHash (String password){
        StringBuffer sb = new StringBuffer();
        byte[]string= new byte[0];
        try {
            string = password.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(string);
            byte[] digestedBytes = md.digest();
            for (int i = 0; i < digestedBytes.length; i++)
                sb.append(Integer.toString((digestedBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        catch(Exception e){
            Log.e("APP","Error while encrypting"); // logar o erro ao encriptar a palavra passe caso ele ocorra
        }
        return sb.toString();
    } */

