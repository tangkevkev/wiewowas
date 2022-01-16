import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;
import database.ChatDescription;
import database.DatabaseInterface;

public class DatabaseTest {

    DatabaseInterface db = DatabaseInterface.getDatabaseInterface();

    @Test
    public void correctness() {
        String[] tags = {"tag1","tag2","tag3"};
        ChatDescription chat1 = new ChatDescription("Test1",45.8,44.7,15,tags);

        //test user creation
        int user_id = db.registerUser("test_corr", "pwd");
        assertEquals("register did not return 1!",1,user_id);
        //test login
        int login = db.login("test_corr", "pwd");
        assertEquals("login did not return 1!",1,login);
        //test chat creation
        int chat_id = db.createChat(chat1);
        assertEquals("chat creation did not return 1!",1,chat_id);
        //test chat joining
        boolean joinChat = db.joinChat(user_id,chat_id);
        assertTrue("join chat did not work!",joinChat);
        //test chat membership check
        boolean isInChat = db.isInChat(user_id,chat_id);
        assertTrue("'is in chat' chat did not work!",isInChat);
        //test chat leaving
        //boolean leaveChat = db.leaveChat(user_id, chat_id);
        //assertTrue("leaving chat did not work!",leaveChat);
        //assertFalse(db.isInChat(user_id,chat_id));
        //test chat retrieval
        //chat 1: joined
        //chat 2: not joined
        //chat 3: joined
        ChatDescription chat2 = new ChatDescription("Test2",45.8,44.7,15,tags);
        ChatDescription chat3 = new ChatDescription("Test3",45.8,44.7,15,tags);
        db.createChat(chat2);
        db.createChat(chat3);
        db.joinChat(1,3);
        LinkedList<ChatDescription> list = db.getAllChats(1);

        boolean[] joined = new boolean[3];
        ChatDescription el;

        for (int i = 0;i<list.size();i++){
            el = list.get(i);
            switch (el.getChatID()){
                case 1: assertTrue(el.isJoined());break;
                case 2: assertFalse(el.isJoined());break;
                case 3: assertTrue(el.isJoined());break;
                default:
            }
            joined[i] = el.isJoined();
        }

        for (int i=0;i<3;i++){
            System.out.println(joined[i]);
        }

        System.out.println("registerUser returned: " + user_id);
        System.out.println("login returned: " + login);
        System.out.println("createChat returned: "+chat_id);
        System.out.println("joinChat returned: "+joinChat);
        System.out.println("isInChat  returned: "+isInChat);
        //System.out.println("leaveChat  returned: "+leaveChat);
        System.out.println("user is no longer in chat after removal");
    }



    public void joinChatBeforeCreation() {
        String[] tags = {"tag1","tag2","tag3"};
        ChatDescription chat = new ChatDescription("Test2",45.8,44.7,15,tags);

        int register = db.registerUser("test_f1", "pwd");
        int login = db.login("test_f1", "pwd");

        boolean joinChat = db.joinChat(register,2); //this should not work!

        int create_chat = db.createChat(chat);
        boolean isInChat = db.isInChat(register,create_chat);

        System.out.println("registerUser returned: " + register);
        System.out.println("login returned: " + login);
        System.out.println("createChat returned: "+create_chat);
        System.out.println("joinChat returned: "+joinChat);
        System.out.println("isInChat  returned: "+isInChat);
    }
}
