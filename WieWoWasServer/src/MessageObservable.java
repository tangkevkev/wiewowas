import database.ChatMessage;

import java.util.HashSet;
import java.util.Set;

/**
 * This is the observable which notifies all registered {@code #MessageSentObserver}.
 */
class MessageObservable {
    private final Set<MessageSentObserver> observers = new HashSet<>();

    synchronized void addObserver(MessageSentObserver mso) {
        observers.add(mso);
    }

    synchronized void deleteObserver(MessageSentObserver mso) {
        observers.remove(mso);
    }

    synchronized void notifyAll(ChatMessage msg) {
        try {
            observers.forEach(observer -> observer.messageSent(msg));
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    interface MessageSentObserver {
        void messageSent(ChatMessage msg);
    }
}
