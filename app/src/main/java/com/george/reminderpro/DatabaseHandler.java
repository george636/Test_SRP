package com.george.reminderpro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	private static DatabaseHandler sInstance = null;
	
	//database version
	private static final int DATABASE_VERSION = 5;
	
	//database name
	private static final String DATABASE_NAME = "remindersManager";
	
	//reminders table name
	private static final String TABLE_REMINDERS = "reminders";
	
	//reminders table column names
	private static final String KEY_ID = "id";
	private static final String KEY_ALARM_ID = "alarmid";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_TIME = "time";
	private static final String KEY_DATE = "date";
	private static final String KEY_TIMESTAMP = "timestamp";
	private static final String KEY_STATUS = "status";
	private static final String KEY_RECURRENCE_POSITION = "recurrenceposition";
	private static final String KEY_ORIGINAL_TIMESTAMP = "originaltimestamp";
	
	// Use the application context, which will ensure that you 
    // don't accidentally leak an Activity's context.
    // See this article for more information: http://bit.ly/6LRzfx
	public static DatabaseHandler getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new DatabaseHandler(context.getApplicationContext());
		}
		return sInstance;
	}

	//constructor
	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	//create database and table
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDERS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_ALARM_ID + " INTEGER," 
				+ KEY_DESCRIPTION + " TEXT," + KEY_TIME + " TEXT," + KEY_DATE 
				+ " TEXT," + KEY_TIMESTAMP + " INTEGER," + KEY_STATUS 
				+ " INTEGER," + KEY_RECURRENCE_POSITION + " INTEGER," + KEY_ORIGINAL_TIMESTAMP + " INTEGER" + ")";	
		db.execSQL(CREATE_REMINDERS_TABLE);
	}
	
	//upgrade database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		if (oldVersion == 4) {
			//alter table and insert new column
			//DONE!			
			upgradeFrom4to5(db);
			oldVersion = 5;
		}
		
		if (oldVersion == 5) {		
			upgradeFrom5to6(db);
			oldVersion = 6;
		}
		
		if (oldVersion == 6) {			
			upgradeFrom6to7(db);
			oldVersion = 7;
		}
		
		if (oldVersion == 7) {		
			upgradeFrom7to8(db);
			oldVersion = 8;
		}
		
		//EXAMPLES OF OTHER ACTIONS
		//2. drop table and create new one
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
		//recreate table
		//onCreate(db);	
	}
	
	private void upgradeFrom4to5(SQLiteDatabase db){
		String ALTER_REMINDERS_TABLE_ADD_COLUMN_RECURRENCE 			= "ALTER TABLE " + TABLE_REMINDERS + " ADD COLUMN " + KEY_RECURRENCE_POSITION + " INTEGER DEFAULT 0";		
		String ALTER_REMINDERS_TABLE_ADD_COLUMN_ORIGINAL_TIMESTAMP 	= "ALTER TABLE " + TABLE_REMINDERS + " ADD COLUMN " + KEY_ORIGINAL_TIMESTAMP + " INTEGER DEFAULT 0";		
		db.execSQL(ALTER_REMINDERS_TABLE_ADD_COLUMN_RECURRENCE);
		db.execSQL(ALTER_REMINDERS_TABLE_ADD_COLUMN_ORIGINAL_TIMESTAMP);
	}
	
	private void upgradeFrom5to6(SQLiteDatabase db){
		//for future use
	}
	
	private void upgradeFrom6to7(SQLiteDatabase db){
		//for future use
	}
	
	private void upgradeFrom7to8(SQLiteDatabase db){
		//for future use
	}

	
	//add new reminder
	public int addReminder(Reminder reminder) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_ALARM_ID, reminder.getAlarmId());
		values.put(KEY_DESCRIPTION, reminder.getDescription());
		values.put(KEY_TIME, reminder.getTime());
		values.put(KEY_DATE, reminder.getDate());
		values.put(KEY_TIMESTAMP, reminder.getTimestamp());
		values.put(KEY_STATUS, reminder.getStatus());
		values.put(KEY_RECURRENCE_POSITION, reminder.getRecurrencePosition());
		values.put(KEY_ORIGINAL_TIMESTAMP, reminder.getOriginalTimestamp());
		
		//create long that will be returned
		int insertedRowId;
		
		//insert row
		insertedRowId = (int) db.insert(TABLE_REMINDERS, null, values);
		db.close();
		
		return insertedRowId;		
	}
	
	//get single reminder
	public Reminder getReminder(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_REMINDERS, new String[] { KEY_ID, KEY_ALARM_ID,
				KEY_DESCRIPTION, KEY_TIME, KEY_DATE, KEY_TIMESTAMP, KEY_STATUS, KEY_RECURRENCE_POSITION, KEY_ORIGINAL_TIMESTAMP }, KEY_ID + "=?", 
				new String[] { String.valueOf(id) }, null, null, null, null);

		if(cursor != null && cursor.moveToFirst()) {
			/*0=id
             *1=alarm id
             *2=description
             *3=time
             *4=date
             *5=time stamp
             *6=status
             *7=recurrence position
             *8=original timestamp
             */

                Reminder reminder = new Reminder(Integer.parseInt(cursor.getString(0)),
                    Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4),
                    Long.parseLong(cursor.getString(5)), Integer.parseInt(cursor.getString(6)),
                    Integer.parseInt(cursor.getString(7)),Long.parseLong(cursor.getString(8)));

            cursor.close();
            db.close();

            return reminder;
		}
        //prevent force close on java null pointer exception
        return new Reminder(0, 0, "", "", "", 0, 0, 0, 0);
	} 
	
	//get all reminders
	public List<Reminder> getAllReminders() {
		List<Reminder> reminderList = new ArrayList<Reminder>();
		
		//select all query, SORT IT FIRST BY STATUS (enabled first) then by DATE
		String selectQuery = "SELECT * FROM " + TABLE_REMINDERS + " ORDER BY " + KEY_STATUS + " DESC, " + KEY_TIMESTAMP + " ASC";
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		//looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				
				Reminder reminder = new Reminder(Integer.parseInt(cursor.getString(0)),
						Integer.parseInt(cursor.getString(1)), cursor.getString(2), 
						cursor.getString(3), cursor.getString(4), 
						Long.parseLong(cursor.getString(5)), Integer.parseInt(cursor.getString(6)),
						Integer.parseInt(cursor.getString(7)),Long.parseLong(cursor.getString(8)));
				
				//insert reminder into the list
				reminderList.add(reminder);
			}
			while (cursor.moveToNext());
		}
		
		cursor.close();
		db.close();
		
		return reminderList;
	}
	
	//get reminders count
	public int getRemindersCount() {
		String countQuery = "SELECT * FROM " + TABLE_REMINDERS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		
		int count = cursor.getCount();
		
		cursor.close();
		db.close();
		
		return count;
	}
	
	//update single reminder
	public int updateReminder(Reminder reminder) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_ALARM_ID, reminder.getAlarmId());
		values.put(KEY_DESCRIPTION, reminder.getDescription());
		values.put(KEY_TIME, reminder.getTime());
		values.put(KEY_DATE, reminder.getDate());
		values.put(KEY_TIMESTAMP, reminder.getTimestamp());
		values.put(KEY_STATUS, reminder.getStatus());
		values.put(KEY_RECURRENCE_POSITION, reminder.getRecurrencePosition());
		values.put(KEY_ORIGINAL_TIMESTAMP, reminder.getOriginalTimestamp());
		
		
		//update row
		int affectedRows = db.update(TABLE_REMINDERS, values, KEY_ID + "=?", 
				new String[] { String.valueOf(reminder.getId()) });
		
		db.close();
		
		return affectedRows;
	}
	
	//delete single reminder
	public void deleteReminder(Reminder reminder) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_REMINDERS, KEY_ID + "=?", 
				new String[] { String.valueOf(reminder.getId()) });
		db.close();
	}
	
	//delete single reminder
	public void deleteReminder(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_REMINDERS, KEY_ID + "=?", 
				new String[] { String.valueOf(id) });
		db.close();
	}
}
