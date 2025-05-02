package com.example.selfbudgettracker;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME="Login.db";

    public DBHelper(Context context) {
        super(context, "Login.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        MyDB.execSQL("create Table users (UserName TEXT primary key, Email TEXT,PhoneNumber TEXT,Password TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int oldVersion, int newVersion) {

        MyDB.execSQL("drop Table if exists users");

    }
    public Boolean insertData(String UserName, String Email, String PhoneNumber,String Password)
    {
        SQLiteDatabase MyDB=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("UserName",UserName);
        contentValues.put("Email",Email);
        contentValues.put("PhoneNumber",PhoneNumber);
        contentValues.put("Password",Password);

        long result=MyDB.insert("users",null,contentValues);
        if(result==-1) return false;
        else
            return true;
    }

    public Boolean checkEmail(String Email)
    {
        SQLiteDatabase MyDB=this.getWritableDatabase();
        Cursor cursor=MyDB.rawQuery("Select * from users where Email=?",new String[] {Email});
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }

    public Boolean checkuserpassword(String Email,String Password)
    {
        SQLiteDatabase MyDB=this.getWritableDatabase();
        Cursor cursor=MyDB.rawQuery("Select * from users where Email=? and Password=?",new String[]{Email,Password});
        if(cursor.getCount()>0)
            return true;
        else
            return false;
    }

    public Cursor getData() {
        SQLiteDatabase DB = this.getWritableDatabase();
        // Query to select records based on the provided email
        Cursor cursor = DB.rawQuery("SELECT * FROM users", null);
        return cursor;
    }
}
