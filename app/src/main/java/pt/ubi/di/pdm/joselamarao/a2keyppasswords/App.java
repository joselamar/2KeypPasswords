package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static pt.ubi.di.pdm.joselamarao.a2keyppasswords.AjudanteParaAbrirBD.TABLE_NAME1;

public class App extends Activity {
    private ImageView iconKey;
    private Toolbar toolbar;
    private AjudanteParaAbrirBD oAPABD;
    private SQLiteDatabase oSQLiteDB;
    private RecyclerView rv;
    private adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SearchView searchView;
    ArrayList<Item> ar = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        //to get username to other intent
        Intent iCameFromActivity1 = getIntent();
        final String intentusername= iCameFromActivity1.getStringExtra("username");

        //para por a imagem na activity Inicio
        iconKey = (ImageView) findViewById(R.id.keyimage);
        iconKey.setImageResource(R.drawable.keyicon);

        oAPABD = new AjudanteParaAbrirBD(this);
        oSQLiteDB = oAPABD.getWritableDatabase();


        //backbutton
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.addbutton);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iActivity = new Intent(getApplicationContext(),AddURL.class);
                iActivity.putExtra("username",intentusername);
                startActivity(iActivity);
                finish();
            }
        });



        rv=(RecyclerView)findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new adapter(ar);

        //Cursor oCursor = oSQLiteDB.query(oAPABD.TABLE_NAME1, new String []{ "*"} ,null , null , null , null , null , null);
        Cursor oCursor = oSQLiteDB.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ?" , new String[]{intentusername});
        boolean bCarryOn = oCursor.moveToFirst();

        while (bCarryOn) {
            ar.add(new Item(oCursor.getString(2),oCursor.getString(6)));
            bCarryOn = oCursor.moveToNext();
        }
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(mAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.my_custom_divider));
        rv.addItemDecoration(divider);

        mAdapter.setOnItemClickListener(new adapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent iCameFromActivity1 = getIntent();
                final String intentusername= iCameFromActivity1.getStringExtra("username");

                Intent iActivity = new Intent(getApplicationContext(),seeEntry.class);
                iActivity.putExtra("username",intentusername);
                iActivity.putExtra("title", ar.get(position).getText1());
                startActivity(iActivity);
                finish();
            }
        });

        searchView=(SearchView) findViewById(R.id.your_icon);
        searchView.setQueryHint("Search ");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                filter(newText.toLowerCase());
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText.toLowerCase());
                return true;
            }
        });


        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped( RecyclerView.ViewHolder viewHolder, int i) {
                if (i == ItemTouchHelper.RIGHT) {
                    int position = viewHolder.getAdapterPosition();
                    String aux=ar.get(position).getText1();
                    Cursor oCursor = oSQLiteDB.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ? and title= ?" , new String[]{intentusername, aux});
                    if(oCursor.moveToFirst()){
                        byte[] encripted = oAPABD.getEncryptedPass(intentusername,aux);
                        byte[] iv = oAPABD.getIv(intentusername,aux);
                        IvParameterSpec ivspec = new IvParameterSpec(iv);
                        byte[] hash=oAPABD.hashedPassword(intentusername);
                        SecretKeySpec key=oAPABD.getKey(hash);
                        try {
                            byte[] decrypt=oAPABD.decrypt(encripted,ivspec,key);
                            final String decript = new String(decrypt);
                            final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", decript);
                            clipboard.setPrimaryClip(clip);
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    ClipData clip1 = ClipData.newPlainText("label", "AHAH nice try!!");
                                    clipboard.setPrimaryClip(clip1);
                                }
                            }, 30000);
                            ar.clear();
                            mLayoutManager = new LinearLayoutManager(App.this);
                            mAdapter = new adapter(ar);

                            //Cursor oCursor = oSQLiteDB.query(oAPABD.TABLE_NAME1, new String []{ "*"} ,null , null , null , null , null , null);
                            Cursor Cursor = oSQLiteDB.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ?" , new String[]{intentusername});
                            boolean bCarryOn = Cursor.moveToFirst();

                            while (bCarryOn) {
                                ar.add(new Item(Cursor.getString(2),oCursor.getString(6)));
                                bCarryOn = Cursor.moveToNext();
                            }
                            rv.setLayoutManager(mLayoutManager);
                            rv.setAdapter(mAdapter);
                            DividerItemDecoration divider = new DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL);
                            divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.my_custom_divider));
                            rv.addItemDecoration(divider);

                            mAdapter.setOnItemClickListener(new adapter.onItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    Intent iCameFromActivity1 = getIntent();
                                    final String intentusername= iCameFromActivity1.getStringExtra("username");

                                    Intent iActivity = new Intent(getApplicationContext(),seeEntry.class);
                                    iActivity.putExtra("username",intentusername);
                                    iActivity.putExtra("title", ar.get(position).getText1());
                                    startActivity(iActivity);
                                    finish();
                                }
                            });
                            Toast.makeText(App.this, "Password copied to clipboard for 30 seconds, be quick", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("TAG", "Error decrypting");
                        }
                    }
                }
                else {
                    int position = viewHolder.getAdapterPosition();
                    final String aux=ar.get(position).getText1();
                    AlertDialog.Builder builder = new AlertDialog.Builder(App.this,R.style.seeEntry);
                    builder.setTitle("Eliminate Entry")
                            .setMessage("Do you realy want to delete this entry")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                oSQLiteDB.delete(TABLE_NAME1, "username = ? AND title = ?", new String[]{intentusername,aux});
                                    ar.clear();
                                    mLayoutManager = new LinearLayoutManager(App.this);
                                    mAdapter = new adapter(ar);

                                    //Cursor oCursor = oSQLiteDB.query(oAPABD.TABLE_NAME1, new String []{ "*"} ,null , null , null , null , null , null);
                                    Cursor oCursor = oSQLiteDB.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ?" , new String[]{intentusername});
                                    boolean bCarryOn = oCursor.moveToFirst();

                                    while (bCarryOn) {
                                        ar.add(new Item(oCursor.getString(2),oCursor.getString(6)));
                                        bCarryOn = oCursor.moveToNext();
                                    }
                                    rv.setLayoutManager(mLayoutManager);
                                    rv.setAdapter(mAdapter);
                                    DividerItemDecoration divider = new DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL);
                                    divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.my_custom_divider));
                                    rv.addItemDecoration(divider);

                                    mAdapter.setOnItemClickListener(new adapter.onItemClickListener() {
                                        @Override
                                        public void onItemClick(int position) {
                                            Intent iCameFromActivity1 = getIntent();
                                            final String intentusername= iCameFromActivity1.getStringExtra("username");

                                            Intent iActivity = new Intent(getApplicationContext(),seeEntry.class);
                                            iActivity.putExtra("username",intentusername);
                                            iActivity.putExtra("title", ar.get(position).getText1());
                                            startActivity(iActivity);
                                            finish();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ar.clear();
                                    mLayoutManager = new LinearLayoutManager(App.this);
                                    mAdapter = new adapter(ar);

                                    //Cursor oCursor = oSQLiteDB.query(oAPABD.TABLE_NAME1, new String []{ "*"} ,null , null , null , null , null , null);
                                    Cursor oCursor = oSQLiteDB.rawQuery("SELECT * FROM " +TABLE_NAME1 + " WHERE username= ?" , new String[]{intentusername});
                                    boolean bCarryOn = oCursor.moveToFirst();

                                    while (bCarryOn) {
                                        ar.add(new Item(oCursor.getString(2),oCursor.getString(6)));
                                        bCarryOn = oCursor.moveToNext();
                                    }
                                    rv.setLayoutManager(mLayoutManager);
                                    rv.setAdapter(mAdapter);
                                    DividerItemDecoration divider = new DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL);
                                    divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.my_custom_divider));
                                    rv.addItemDecoration(divider);

                                    mAdapter.setOnItemClickListener(new adapter.onItemClickListener() {
                                        @Override
                                        public void onItemClick(int position) {
                                            Intent iCameFromActivity1 = getIntent();
                                            final String intentusername= iCameFromActivity1.getStringExtra("username");

                                            Intent iActivity = new Intent(getApplicationContext(),seeEntry.class);
                                            iActivity.putExtra("username",intentusername);
                                            iActivity.putExtra("title", ar.get(position).getText1());
                                            startActivity(iActivity);
                                            finish();
                                        }
                                    });
                                }
                            });
                    builder.show();
                }

            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    if (dX > 0) {

                        Rect r = new Rect(itemView.getLeft(), itemView.getTop(), (int)dX, itemView.getBottom());
                        drawRectText("Copy password to Clipboard",c,r,p);

                    }
                    else {
                        Rect r = new Rect(itemView.getRight()+(int)dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        drawRectText("Eliminate Entry",c,r,p);
                    }


                    float alpha = (float)1.0- Math.abs(dX)/(float) itemView.getWidth();
                    itemView.setAlpha(alpha);
                    itemView.setTranslationX(dX);

                    super.onChildDraw(c, recyclerView, viewHolder,dX, dY, actionState, isCurrentlyActive);
                }

            }

        });

        helper.attachToRecyclerView(rv);
    }

    private void drawRectText(String text, Canvas canvas, Rect r, Paint textPaint) {
        textPaint.setTextSize(50);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int width = r.width();

        int numOfChars = textPaint.breakText(text,true,width,null);
        int start = (text.length()-numOfChars)/2;
        canvas.drawText(text,start,start+numOfChars,r.exactCenterX(),r.exactCenterY(),textPaint);
    }

    protected void filter(String text){
        ArrayList<Item> list= new ArrayList<>();
        for(Item item : ar){
            if(item.getText1().toLowerCase().contains(text))
                list.add(item);
            if(item.getText2().toLowerCase().contains(text)){
                if(list.contains(item))
                    continue;
                else list.add(item);
            }
        }
        mAdapter.searchedlist(list);
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

}
