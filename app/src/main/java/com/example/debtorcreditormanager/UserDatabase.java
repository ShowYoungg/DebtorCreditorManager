package com.example.debtorcreditormanager;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {CustomerRecord.class, Customer.class}, version = 3, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private static UserDatabase INSTANCE;
    private static final String DATABASE_NAME = "customer_database";
    private static Object lock = new Object();
    public abstract UserDao mDao();

    public static UserDatabase getInstance(Context context){
        if (INSTANCE == null) {
            synchronized (lock) {
                if (INSTANCE == null) {
                    //create database Here......
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UserDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            //.addMigrations(MIGRATION_1_2)
                            .build();

                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };
}