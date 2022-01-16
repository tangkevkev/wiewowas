package database;

/*
 * Created by Tobias P, November/December 2017
 * Manages server communication with database
 */

import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseInterface {
    private static final DatabaseInterface DB_IC = new DatabaseInterface();
    private Connection conn;
    //database locks
    private final ReentrantReadWriteLock usr_lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock chats_lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock msgs_lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock members_lock = new ReentrantReadWriteLock();
    //Strings for SQL queries
    private String USER_FETCH =
            "SELECT * FROM " + DBStrings.TABLE_USERS + " WHERE " + DBStrings.USERS_NAME + " = ?";
    private String USER_INSERT =
            "INSERT INTO " + DBStrings.TABLE_USERS
                    + " ('" + DBStrings.USERS_NAME
                    + "','" + DBStrings.USERS_PWD
                    + "') VALUES (?,?)";
    private String USR_NAME_LOOKUP =
            "SELECT " + DBStrings.USERS_NAME + " FROM " + DBStrings.TABLE_USERS + " WHERE "
                    + DBStrings.USERS_ID + " = ?";
    private String USR_LOC_SET =
            "UPDATE " + DBStrings.TABLE_USERS + " SET " + DBStrings.USERS_LOC_DESCR + " = ?, "
            + DBStrings.USERS_LOC_LAT + " = ?, " + DBStrings.USERS_LOC_LONG + " = ?, "
            + DBStrings.USERS_LOC_SHARE + " = 1 WHERE " + DBStrings.USERS_ID + " = ?";
    private String USR_SET_SHARE =
            "UPDATE " + DBStrings.TABLE_USERS + " SET " + DBStrings.USERS_LOC_SHARE + " = ? WHERE "
                    + DBStrings.USERS_ID + " = ?";
    private String MSG_INSERT =
            "INSERT INTO " + DBStrings.TABLE_MSG
                    + " ('" + DBStrings.MSG_CHAT_ID
                    + "','" + DBStrings.MSG_USER_ID
                    + "','" + DBStrings.MSG_USER_NAME
                    + "','" + DBStrings.MSG_CONTENT
                    + "','" + DBStrings.MSG_TIME
                    + "') VALUES (?,?,?,?,?)";
    private String MSG_READ =
            "SELECT * FROM " + DBStrings.TABLE_MSG + " WHERE " + DBStrings.MSG_CHAT_ID + " = ? AND "
                    + DBStrings.MSG_TIME + " >= ? ORDER BY " + DBStrings.MSG_TIME;
    private String MEMBER_INSERT =
            "INSERT INTO " + DBStrings.TABLE_CHAT_MEMBERS
                    + " ('" + DBStrings.CHAT_MEMBERS_CHAT_ID
                    + "','" + DBStrings.CHAT_MEMBERS_USER_ID
                    + "') VALUES (?,?)";
    private String MEMBER_REMOVE =
            "DELETE FROM " + DBStrings.TABLE_CHAT_MEMBERS + " WHERE " + DBStrings.CHAT_MEMBERS_CHAT_ID
                    + " = ? AND " + DBStrings.CHAT_MEMBERS_USER_ID + " = ?";
    private String MEMBER_CHECK =
            "SELECT * FROM " + DBStrings.TABLE_CHAT_MEMBERS + " WHERE " + DBStrings.CHAT_MEMBERS_USER_ID
                    + " = ? AND " + DBStrings.CHAT_MEMBERS_CHAT_ID + " = ?";
    private String CHATS_JOINED =
            "SELECT " + DBStrings.TABLE_CHATS + ".* FROM " + DBStrings.TABLE_CHATS + " JOIN (SELECT "
                    + DBStrings.CHATS_ID + " FROM " + DBStrings.TABLE_CHAT_MEMBERS + " WHERE " +
                    DBStrings.CHAT_MEMBERS_USER_ID + " = ?) AS T ON " + DBStrings.TABLE_CHATS + "." +
                    DBStrings.CHATS_ID + " = T." + DBStrings.CHAT_MEMBERS_CHAT_ID;
    private String CHATS_NEW =
            "SELECT a.* FROM " + DBStrings.TABLE_CHATS + " AS a LEFT JOIN (" + CHATS_JOINED + ") AS b ON a." +
                    DBStrings.CHATS_ID + " = b." + DBStrings.CHATS_ID + " WHERE b." + DBStrings.CHATS_ID + " IS NULL";
    private String CHATS_INSERT =
            "INSERT INTO " + DBStrings.TABLE_CHATS
                    + " ('" + DBStrings.CHATS_NAME
                    + "','" + DBStrings.CHATS_TAG1
                    + "','" + DBStrings.CHATS_TAG2
                    + "','" + DBStrings.CHATS_TAG3
                    + "','" + DBStrings.CHATS_LOCATION_LONG
                    + "','" + DBStrings.CHATS_LOCATION_LAT
                    + "','" + DBStrings.CHATS_RANGE
                    + "') VALUES (?,?,?,?,?,?,?)";
    private String CHATS_FETCH_NEWEST =
            "SELECT MAX(" + DBStrings.CHATS_ID + ") FROM " + DBStrings.TABLE_CHATS;
    private String CHATS_FETCH_LOCS =
            "SELECT " + DBStrings.USERS_NAME + ", " + DBStrings.USERS_LOC_DESCR
            + ", " + DBStrings.USERS_LOC_LAT + ", " + DBStrings.USERS_LOC_LONG
            + "  FROM " + DBStrings.TABLE_USERS + " JOIN " + DBStrings.TABLE_CHAT_MEMBERS
            + " ON " + DBStrings.TABLE_USERS + "." + DBStrings.USERS_ID + " = "
            + DBStrings.TABLE_CHAT_MEMBERS + "." + DBStrings.CHAT_MEMBERS_USER_ID + " WHERE "
            + DBStrings.TABLE_CHAT_MEMBERS + "." + DBStrings.CHAT_MEMBERS_CHAT_ID + " = ? AND "
            + DBStrings.USERS_LOC_SHARE + " = 1";

    /**
     * Do not instantiate this class. Use {@link #getDatabaseInterface()} instead.
     * This enforces the singleton pattern.
     */
    private DatabaseInterface() {
        conn = this.connect(); //establish Connection object
        boolean worked = createTables(); //create all DB tables
        if (!worked){
            System.err.println("Creating DB tables failed!");
        }
    }

    public static DatabaseInterface getDatabaseInterface() {
        return DB_IC;
    }

    private Connection connect() {
        String url = "jdbc:sqlite:wiewowas.db";
        Connection conn = null;
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            conn = DriverManager.getConnection(url, config.toProperties());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Creates all database tables
     *
     * @return true if successful, false otherwise
     */

    private boolean createTables() {
        try (Statement creation = conn.createStatement()) {
            creation.execute(DBStrings.TABLE_USERS_CREATE);
            creation.execute(DBStrings.TABLE_CHATS_CREATE);
            creation.execute(DBStrings.TABLE_CHAT_MEMBERS_CREATE);
            creation.execute(DBStrings.TABLE_MSG_CREATE);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registers a new user
     *
     * @return -1 if unsuccessful. Otherwise, the unique ID of the newly registered user
     */
    public int registerUser(String username, String pwd) {
        usr_lock.writeLock().lock();

        try (PreparedStatement usr_check = conn.prepareStatement(USER_FETCH);
             PreparedStatement pstmt = conn.prepareStatement(USER_INSERT)) {

            usr_check.setString(1, username); //include username in SQL query
            ResultSet res = usr_check.executeQuery(); //query for username
            if (res.isBeforeFirst()) { //result set is non-empty => user already exists => error
                res.close();
                return -1;
            } else { //create user
                pstmt.setString(1, username);
                pstmt.setString(2, pwd);
                pstmt.executeUpdate(); //insert into DB
                ResultSet id = usr_check.executeQuery(); //check for & return user id
                id.next();
                return id.getInt(DBStrings.USERS_ID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            usr_lock.writeLock().unlock();
        }
    }

    /**
     * Checks a users login credentials
     *
     * @param username the username to be checked
     * @param password the user password to be checked
     * @return -1 if credentials are false. Otherwise, the ID of the user that has logged in succesfully
     */
    public int login(String username, String password) {
        usr_lock.readLock().lock();

        try (PreparedStatement usr_check = conn.prepareStatement(USER_FETCH)) {

            usr_check.setString(1, username); //include username in SQL query
            ResultSet res = usr_check.executeQuery(); //returns user table line containing user with 'username'
            if (!res.isBeforeFirst()) {
                return -1; //result set is empty => user doesn't exist
            } else if (password.equals(res.getString(DBStrings.USERS_PWD))) { //check credentials
                return res.getInt(DBStrings.USERS_ID); //credentials correct, look up & return user_id
            } else { //credentials incorrect
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            usr_lock.readLock().unlock();
        }
    }

    /**
     * Returns whether a user is in a given chat
     *
     * @param user_id User ID getting checked
     * @param chat_id Chat ID to be checked
     * @return true if user is in chat, false otherwise
     */
    public boolean isInChat(int user_id, int chat_id) {
        members_lock.readLock().lock();

        try (PreparedStatement check = conn.prepareStatement(MEMBER_CHECK)) {
            check.setInt(1, user_id);
            check.setInt(2, chat_id);
            ResultSet res = check.executeQuery();
            return res.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return isInChat(user_id, chat_id);
        } finally {
            members_lock.readLock().unlock();
        }
    }

    /**
     * Sends a new message into an existing chat
     *
     * @param chat_id the ID of the chat the message is being sent in
     * @param user_id the ID of the user sending the message
     * @param content the content of the message
     * @return true if message was stored successfully, false otherwise
     */
    public boolean storeTextMessage(int user_id, int chat_id, String content) {
        msgs_lock.writeLock().lock();
        usr_lock.readLock().lock();

        try (PreparedStatement msg_ins = conn.prepareStatement(MSG_INSERT);
             PreparedStatement usr_name = conn.prepareStatement(USR_NAME_LOOKUP)) {
            usr_name.setInt(1, user_id); //look up user name using ID
            ResultSet name = usr_name.executeQuery();
            name.next();

            msg_ins.setInt(1, chat_id);
            msg_ins.setInt(2, user_id);
            msg_ins.setString(3, name.getString(DBStrings.USERS_NAME));
            msg_ins.setString(4, content);
            msg_ins.setLong(5, System.currentTimeMillis());
            msg_ins.executeUpdate(); //insert into DB
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            msgs_lock.writeLock().unlock();
            usr_lock.readLock().unlock();
        }
    }

    /**
     * Get all chats for a particular user
     *
     * @param user_id the user which requests the list. The field {@link ChatDescription#joined}
     *                has to be set according to whether the user has joined the chat
     * @return null if unsuccessful, a linked list of all chats otherwise, containing joined chats
     * first, then 'unjoined' chats
     */
    public LinkedList<ChatDescription> getAllChats(int user_id) {
        int id;
        double longitude;
        double latitude;
        int radius;
        LinkedList<ChatDescription> result = new LinkedList<>();

        chats_lock.readLock().lock();
        members_lock.readLock().lock();

        try (PreparedStatement joined_chats = conn.prepareStatement(CHATS_JOINED);
             PreparedStatement new_chats = conn.prepareStatement(CHATS_NEW)) {

            joined_chats.setInt(1, user_id); //include user_id in SQL statements
            new_chats.setInt(1, user_id);
            ResultSet joined_c = joined_chats.executeQuery(); //returns all joined chats
            ResultSet new_c = new_chats.executeQuery(); //returns all non-joined chats

            while (joined_c.next()) { //create chat for each result of 'joined' query
                String name = joined_c.getString(DBStrings.CHATS_NAME);
                longitude = joined_c.getDouble(DBStrings.CHATS_LOCATION_LONG);
                latitude = joined_c.getDouble(DBStrings.CHATS_LOCATION_LAT);
                radius = joined_c.getInt(DBStrings.CHATS_RANGE);
                String[] tags = new String[3];
                tags[0] = joined_c.getString(DBStrings.CHATS_TAG1);
                tags[1] = joined_c.getString(DBStrings.CHATS_TAG2);
                tags[2] = joined_c.getString(DBStrings.CHATS_TAG3);
                id = joined_c.getInt(DBStrings.CHATS_ID);
                result.add(new ChatDescription(name, longitude, latitude, radius, true, tags, id));
            }

            while (new_c.next()) { //create chat for each result of 'non-joined' query
                String name = new_c.getString(DBStrings.CHATS_NAME);
                longitude = new_c.getDouble(DBStrings.CHATS_LOCATION_LONG);
                latitude = new_c.getDouble(DBStrings.CHATS_LOCATION_LAT);
                radius = new_c.getInt(DBStrings.CHATS_RANGE);
                String[] tags = new String[3];
                tags[0] = new_c.getString(DBStrings.CHATS_TAG1);
                tags[1] = new_c.getString(DBStrings.CHATS_TAG2);
                tags[2] = new_c.getString(DBStrings.CHATS_TAG3);
                id = new_c.getInt(DBStrings.CHATS_ID);
                result.add(new ChatDescription(name, longitude, latitude, radius, false, tags, id));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            chats_lock.readLock().unlock();
            members_lock.readLock().unlock();
        }
    }

    /**
     * Fetches all messages after 'timestamp' from chat with ID chat_id
     *
     * @param chat_id   the chat from which to fetch the messages
     * @param timestamp the limit for fetching messages - only newer ones are returned
     * @return a linked list with all requested messages, ordered by timestamp (oldest first)
     */
    public LinkedList<ChatMessage> getMessages(int chat_id, long timestamp) {
        LinkedList<ChatMessage> msgs = new LinkedList<>();

        msgs_lock.readLock().lock();
        try (PreparedStatement msg_read = conn.prepareStatement(MSG_READ)) {
            msg_read.setInt(1, chat_id);
            msg_read.setLong(2, timestamp);
            ResultSet res = msg_read.executeQuery();
            while (res.next()) {
                msgs.add(new ChatMessage(chat_id, res.getString(DBStrings.MSG_CONTENT),
                        res.getString(DBStrings.MSG_USER_NAME), res.getLong(DBStrings.MSG_TIME)));
            }
            return msgs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            msgs_lock.readLock().unlock();
        }
    }

    /**
     * Allows a user to join a Chat
     *
     * @param user_id: The ID of the user wanting to join
     * @param chat_id: The Chat ID of the chat the user wants to join
     * @return true if join successful, false otherwise
     */
    public boolean joinChat(int user_id, int chat_id) {
        members_lock.writeLock().lock();
        try (PreparedStatement memb_ins = conn.prepareStatement(MEMBER_INSERT)) {
            memb_ins.setInt(1, chat_id);
            memb_ins.setInt(2, user_id);
            memb_ins.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            members_lock.writeLock().unlock();
        }
    }

    /**
     * Create a new chat.
     *
     * @param chat contains longitude, latitude, radius and chat name
     * @return the new chat's ID if chat creation succeeded, -1 otherwise (e.g. if chat name already in use)
     */
    public int createChat(ChatDescription chat) {
        // latitude/longitude as described here:
        // https://developer.android.com/reference/android/location/Location.html
        chats_lock.writeLock().lock();
        String[] tags = chat.getTags();

        try (PreparedStatement chats_ins = conn.prepareStatement(CHATS_INSERT);
             PreparedStatement chats_newest = conn.prepareStatement(CHATS_FETCH_NEWEST)) {
            chats_ins.setString(1, chat.getChatName());
            chats_ins.setString(2, tags[0]);
            chats_ins.setString(3, tags[1]);
            chats_ins.setString(4, tags[2]);
            chats_ins.setDouble(5, chat.getLongitude());
            chats_ins.setDouble(6, chat.getLatitude());
            chats_ins.setInt(7, chat.getRadius());
            chats_ins.executeUpdate();
            ResultSet res = chats_newest.executeQuery();//get chat ID of newest chat
            res.next();
            return res.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            chats_lock.writeLock().unlock();
        }
    }

    /**
     * Removes a user from a chat
     *
     * @param chat_id The chat the user wishes to leave
     * @param user_id The user wishing to leave a chat
     * @return true if successful, false otherwise
     */
    public boolean leaveChat(int user_id, int chat_id) {
        members_lock.writeLock().lock();

        try (PreparedStatement leave = conn.prepareStatement(MEMBER_REMOVE)) {
            leave.setInt(1, chat_id);
            leave.setInt(2, user_id);
            leave.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            members_lock.writeLock().unlock();
        }
    }

    /**
     * Stores the location of the user
     *
     * @param usr_id            id of user sharing their location
     * @param loc_description   description of the shared location
     * @param usr_lat           current latitude of the user
     * @param usr_long          current longitude of the user
     * @return                  true if successful, false otherwise
     */
    public boolean storeLocation(int usr_id, String loc_description, double usr_lat, double usr_long) {
        usr_lock.writeLock().lock();

        try(PreparedStatement usr_loc = conn.prepareStatement(USR_LOC_SET)) {
            usr_loc.setString(1,loc_description);
            usr_loc.setDouble(2,usr_lat);
            usr_loc.setDouble(3,usr_long);
            usr_loc.setInt(4,usr_id);
            usr_loc.executeUpdate();
            return true;
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        finally{
            usr_lock.writeLock().unlock();
        }
    }

    /**
     * Sets the location sharing flag for a user
     * @param usr_id    the ID of the user updating their flag
     * @param share     the value the flag should be set to
     * @return          true if successful, false otherwise
     */

    public boolean set_loc_sharing (int usr_id, boolean share){
        usr_lock.writeLock().lock();

        try(PreparedStatement set_loc = conn.prepareStatement(USR_SET_SHARE)){
            if (share){
                set_loc.setInt(1,1);
            }
            else {
                set_loc.setInt(1,0);
            }
            set_loc.setInt(2,usr_id);
            set_loc.executeUpdate();
            return true;
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        finally {
            usr_lock.writeLock().unlock();
        }

    }

    /**
     * Returns the locations of all chat members having switched on sharing
     * @param chat_id   id of chat of which to return user locations
     * @return          array of locations of users that share their location, null if error
     *
     */
    public LinkedList<UserLocation> getLocations(int chat_id) {
        usr_lock.readLock().lock();
        members_lock.readLock().lock();
        LinkedList<UserLocation> list = new LinkedList<>();
        double latitude;
        double longitude;

        try(PreparedStatement set_loc = conn.prepareStatement(CHATS_FETCH_LOCS)){
            set_loc.setInt(1,chat_id);
            ResultSet res = set_loc.executeQuery();
            while (res.next()){
                //userLocation is {name, descr, lat, long}
                String usr_name = res.getString(DBStrings.USERS_NAME);
                String loc_descr = res.getString(DBStrings.USERS_LOC_DESCR);
                latitude = res.getDouble(DBStrings.USERS_LOC_LAT);
                longitude = res.getDouble(DBStrings.USERS_LOC_LONG);
                UserLocation loc = new UserLocation(usr_name, loc_descr,latitude,longitude);
                list.add(loc);
            }
            return list;
        }
        catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        finally {
            usr_lock.readLock().unlock();
            members_lock.readLock().unlock();
        }
    }
}