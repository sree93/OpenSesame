package com.sreemenon.sqlite;


import android.content.ContentValues;
import android.content.Context;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

public class DBTransactions {

    private SreeSqliteHelper helper;

    public DBTransactions(Context context){
        helper = new SreeSqliteHelper(context);
    }

    public Cursor getCursor(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit){
        SQLiteDatabase db = helper.getDb();
        return db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor getCompleteCursor(boolean distinct, String table, String orderBy){
        SQLiteDatabase db = helper.getDb();
        return getCursor(distinct, table, null, null, null, null, null, orderBy, null);
    }

    public void deleteId(String id){
        SQLiteDatabase db = helper.getDb();
        db.delete("gems", "_id =  ?", new String[]{id});
        db.close();
    }

    public void insertData(String table, ContentValues insertValues){
        SQLiteDatabase db = helper.getDb();
        db.insert(table, null, insertValues);
        db.close();
    }

    public void update(String table, ContentValues values, String whereClause, String[] whereArgs){
        SQLiteDatabase db = helper.getDb();
        db.update(table, values, whereClause, whereArgs);
    }

    public void closeDB(){
        helper.closeSqlDB();
    }
}
