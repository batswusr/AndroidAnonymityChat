package com.batsw.anonimitychat.persistence.operations;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.batsw.anonimitychat.persistence.entities.DBMyProfileEntity;
import com.batsw.anonimitychat.persistence.util.IDbEntity;
import com.batsw.anonimitychat.persistence.util.IEntityDbOperations;
import com.batsw.anonimitychat.persistence.util.PersistenceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tudor on 5/4/2017.
 */

public class DbMyProfileOperations implements IEntityDbOperations {

    private static final String LOG = DbMyProfileOperations.class.getSimpleName();

    private SQLiteDatabase mSQLiteDatabase;

    public DbMyProfileOperations(SQLiteDatabase sqLiteDatabase) {
        mSQLiteDatabase = sqLiteDatabase;
    }

    /**
     * This should return at MOST 1 registry
     *
     * @return
     */
    @Override
    public List<IDbEntity> getAllIDbEntity() {
        Log.i(LOG, "getAllIDbEntity -> ENTER");

        List<IDbEntity> retVal = new ArrayList<>();

        String selectQuery = "SELECT " + PersistenceConstants.COLUMN_ID + ", " +
                PersistenceConstants.COLUMN_MY_ADDRESS + ", " +
                PersistenceConstants.COLUMN_MY_NAME + ", " +
                PersistenceConstants.COLUMN_MY_NICKNAME + ", " +
                PersistenceConstants.COLUMN_MY_EMAIL +
                " FROM " + PersistenceConstants.TABLE_MY_PROFILE;

        Cursor cursor = mSQLiteDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DBMyProfileEntity myContact = new DBMyProfileEntity();
                myContact.setId(Long.parseLong(cursor.getString(0)));
                myContact.setMyAddress(cursor.getString(1));
                myContact.setMyName(cursor.getString(2));
                myContact.setMyNickName(cursor.getString(3));
                myContact.setMyEmail(cursor.getString(4));

                retVal.add(myContact);
            } while (cursor.moveToNext());
        }

        Log.i(LOG, "getAllIDbEntity -> LEAVE retVal=" + retVal);
        return retVal;
    }

    /**
     * In this case the  session Id is the ID
     *
     * @param sessionId
     * @return
     */
    @Override
    public IDbEntity getIDbEntityById(long sessionId) {
        Log.i(LOG, "getIDbEntityById -> ENTER sessionId=" + sessionId);
        Log.i(LOG, "Not implemented");
        Log.i(LOG, "getIDbEntityById -> LEAVE retVal=" + null);
        return null;
    }

    @Override
    public boolean addDbEntity(IDbEntity dbEntity) {
        Log.i(LOG, "addDbEntity -> ENTER dbEntity=" + dbEntity);

        boolean retVal = false;

        if (dbEntity instanceof DBMyProfileEntity) {

            DBMyProfileEntity dbMyProfileEntity = (DBMyProfileEntity) dbEntity;

            ContentValues values = new ContentValues();
            values.put(PersistenceConstants.COLUMN_MY_ADDRESS, dbMyProfileEntity.getMyAddress());
            values.put(PersistenceConstants.COLUMN_MY_NAME, dbMyProfileEntity.getMyName());
            values.put(PersistenceConstants.COLUMN_MY_NICKNAME, dbMyProfileEntity.getMyNickName());
            values.put(PersistenceConstants.COLUMN_MY_EMAIL, dbMyProfileEntity.getMyEmail());

            mSQLiteDatabase.insert(PersistenceConstants.TABLE_MY_PROFILE, null, values);
            mSQLiteDatabase.close();

            retVal = true;
        }

        Log.i(LOG, "addDbEntity -> LEAVE retVal=" + retVal);
        return retVal;
    }

    @Override
    public int updateDbEntity(IDbEntity dbEntity) {
        Log.i(LOG, "updateMyContact -> ENTER myProfile=" + dbEntity);
        int retVal = -1;

        if (dbEntity instanceof DBMyProfileEntity) {

            DBMyProfileEntity myProfile = (DBMyProfileEntity) dbEntity;

            if (myProfile.getMyName() != null && !myProfile.getMyName().isEmpty()) {
                if (myProfile.getMyNickName() == null || myProfile.getMyNickName().isEmpty()) {
                    myProfile.setMyNickName(myProfile.getMyName());
                }

                ContentValues values = new ContentValues();
                values.put(PersistenceConstants.COLUMN_MY_NAME, myProfile.getMyName());
                values.put(PersistenceConstants.COLUMN_MY_NICKNAME, myProfile.getMyNickName());
                values.put(PersistenceConstants.COLUMN_MY_EMAIL, myProfile.getMyEmail());

                retVal = mSQLiteDatabase.update(PersistenceConstants.TABLE_MY_PROFILE, values, PersistenceConstants.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(myProfile.getId())});
            } else {
                retVal = -1;
            }

        } else {
            retVal = -1;
        }

        Log.i(LOG, "updateMyContact -> LEAVE retVal=" + retVal);
        return retVal;
    }

//    This is not used in this case

    @Override
    public boolean deleteDbEntity(IDbEntity dbEntity) {
        Log.i(LOG, "deleteDbEntity -> ENTER dbEntity=" + dbEntity);

        Log.i(LOG, "NOT IMPLEMENTED");

        //        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(PersistenceConstants.TABLE_MY_PROFILE, KEY_ID + " = ?",
//                new String[]{String.valueOf(myProfile.getId())});
//        db.close();

        Log.i(LOG, "deleteDbEntity -> LEAVE");

        return false;
    }
}