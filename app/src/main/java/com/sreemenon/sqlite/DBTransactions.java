package com.sreemenon.sqlite;


import android.content.ContentValues;
import android.content.Context;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

/**
 * Wrapper class for SqlDatabase transactions
 */
public class DBTransactions {
    private SreeSqliteHelper helper;

    /**
     * Constructor
     *
     * @param context the context to instantiate the SqliteHelper variable
     */
    public DBTransactions(Context context){
        helper = new SreeSqliteHelper(context);
    }

    /**
     * WrapperCAlss for query()
     *
     * @return Cursor variable of the result
     */
    public Cursor getCursor(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit){
        SQLiteDatabase db = helper.getDb();
        return db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Get complete table with no condition
     *
     * @param distinct boolean for distinct rows
     * @param table table name
     * @param orderBy orderby column names
     * @return
     */
    public Cursor getCompleteCursor(boolean distinct, String table, String orderBy){
        SQLiteDatabase db = helper.getDb();
        return getCursor(distinct, table, null, null, null, null, null, orderBy, null);
    }

    /**
     * Delete a row with a certain ID
     *
     * @param id the id of the row to be deleted
     */
    public void deleteId(String id){
        SQLiteDatabase db = helper.getDb();
        db.delete("gems", "_id =  ?", new String[]{id});
        db.close();
    }

    /**
     * Wrapper for insert
     *
     * @param table table name
     * @param insertValues column values
     */
    public void insertData(String table, ContentValues insertValues){
        SQLiteDatabase db = helper.getDb();
        db.insert(table, null, insertValues);
        db.close();
    }

    /**
     * Wrapper for update
     *
     * @param table table name
     * @param values column values
     * @param whereClause condition
     * @param whereArgs parameters in where clause
     */
    public void update(String table, ContentValues values, String whereClause, String[] whereArgs){
        SQLiteDatabase db = helper.getDb();
        db.update(table, values, whereClause, whereArgs);
    }

    /**
     * Close Database
     */
    public void closeDB(){
        helper.closeSqlDB();
    }
}
