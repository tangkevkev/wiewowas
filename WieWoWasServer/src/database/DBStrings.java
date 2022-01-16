package database;

/**
 * Created by Tobias Petter on 22.11.17
 * Based on work by Tobias Aeschbacher
 * Holds strings describing server database
 */

class DBStrings {
    static final String DATABASE_NAME = "wiewowas.db";
    //static final int DATABASE_VERSION = 1;

    // TABLE USERS
    static final String TABLE_USERS = "users";
    static final String USERS_ID = "user_uid";
    static final String USERS_NAME = "user_name";
    static final String USERS_PWD = "user_pwd";
    static final String USERS_LOC_LONG = "user_loc_long";
    static final String USERS_LOC_LAT = "user_loc_lat";
    static final String USERS_LOC_DESCR = "user_loc_descr";
    static final String USERS_LOC_SHARE = "user_loc_share";
    //static final int USERS_COLUMN_COUNT = 6;
    static final String TABLE_USERS_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                    + USERS_ID + " INTEGER PRIMARY KEY, "
                    + USERS_NAME + " TEXT NOT NULL, "
                    + USERS_PWD + " TEXT NOT NULL, "
                    + USERS_LOC_LONG + " DOUBLE, "
                    + USERS_LOC_LAT + " DOUBLE, "
                    + USERS_LOC_DESCR + " TEXT, "
                    + USERS_LOC_SHARE + " BOOLEAN);";
    //static final String TABLE_USERS_DROP = "DROP TABLE IF EXISTS " + TABLE_USERS;

    // TABLE CHATS
    static final String TABLE_CHATS = "chats";
    static final String CHATS_ID = "chat_uid";
    static final String CHATS_NAME = "chat_name";
    static final String CHATS_TAG1 = "chat_tag1";
    static final String CHATS_TAG2 = "chat_tag2";
    static final String CHATS_TAG3 = "chat_tag3";
    static final String CHATS_LOCATION_LONG = "chat_loc_long";
    static final String CHATS_LOCATION_LAT = "chat_loc_lat";
    static final String CHATS_RANGE = "chat_range";
    //static final int CHATS_COLUMN_COUNT = 8;
    static final String TABLE_CHATS_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CHATS + " ("
                    + CHATS_ID + " INTEGER PRIMARY KEY, "
                    + CHATS_NAME + " TEXT NOT NULL, "
                    + CHATS_TAG1 + " TEXT, "
                    + CHATS_TAG2 + " TEXT, "
                    + CHATS_TAG3 + " TEXT, "
                    + CHATS_LOCATION_LONG + " DOUBLE, "
                    + CHATS_LOCATION_LAT + " DOUBLE, "
                    + CHATS_RANGE + " INTEGER);";
    //static final String TABLE_CHATS_DROP = "DROP TABLE IF EXISTS " + TABLE_CHATS;

    // TABLE MEMBERS
    static final String TABLE_CHAT_MEMBERS = "chat_members";
    static final String CHAT_MEMBERS_ID = "chat_member_uid";
    static final String CHAT_MEMBERS_CHAT_ID = "chat_uid";
    static final String CHAT_MEMBERS_USER_ID = "user_uid";
    //static final int CHAT_MEMBERS_COLUMN_COUNT = 3;
    static final String TABLE_CHAT_MEMBERS_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT_MEMBERS + " ("
                    + CHAT_MEMBERS_ID + " INTEGER PRIMARY KEY, "
                    + CHAT_MEMBERS_CHAT_ID + " INTEGER, "
                    + CHAT_MEMBERS_USER_ID + " INTEGER, "
                    + "FOREIGN KEY (" + CHAT_MEMBERS_CHAT_ID + ") "
                    + "REFERENCES " + TABLE_CHATS + "(" + CHATS_ID + ") "
                    + "ON DELETE CASCADE, "
                    + "FOREIGN KEY (" + CHAT_MEMBERS_USER_ID + ") "
                    + "REFERENCES " + TABLE_USERS + "(" + USERS_ID + "));";
    //static final String TABLE_CHAT_MEMBERS_DROP = "DROP TABLE IF EXISTS " + TABLE_CHAT_MEMBERS;

    // TABLE MESSAGES
    static final String TABLE_MSG = "messages";
    static final String MSG_ID = "message_uid";
    static final String MSG_CHAT_ID = "chat_uid";
    static final String MSG_USER_ID = "user_uid";
    static final String MSG_USER_NAME = "user_name";
    static final String MSG_CONTENT = "message_text";
    static final String MSG_TIME = "message_time";
    //static final int MSG_COLUMN_COUNT = 6;
    static final String TABLE_MSG_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MSG + " ("
                    + MSG_ID + " INTEGER PRIMARY KEY, "
                    + MSG_CHAT_ID + " INTEGER, "
                    + MSG_USER_ID + " INTEGER, "
                    + MSG_USER_NAME + " TEXT, "
                    + MSG_CONTENT + " TEXT, "
                    + MSG_TIME + " INTEGER, "
                    + "FOREIGN KEY (" + MSG_CHAT_ID + ") "
                    + "REFERENCES " + TABLE_CHATS + "(" + CHATS_ID + ") "
                    + "ON DELETE CASCADE, "
                    + "FOREIGN KEY (" + MSG_USER_ID + ") "
                    + "REFERENCES " + TABLE_USERS + "(" + USERS_ID + "));";
    //static final String TABLE_MSG_DROP = "DROP TABLE IF EXISTS " + TABLE_MSG;
}