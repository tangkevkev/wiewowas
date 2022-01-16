package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.project.forstesa.wiewowas.R;

/**
 * Created by dell on 11/22/17.
 */

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;
    private String author;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatMessageAdapter(Context context, int textViewResourceId, @NonNull String author) {
        super(context, textViewResourceId);
        this.context = context;
        this.author = author;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage= getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessage.belongsTo(author)) {
            row = inflater.inflate(R.layout.msg_right, parent, false);
        }else{
            row = inflater.inflate(R.layout.msg_left, parent, false);
        }
        String message = chatMessage.getContent();
        String tmp = new Timestamp(chatMessage.getTimeInMillis()).toString();
        String tmsp = tmp.substring(0, 19);
        SpannableString styledString = new SpannableString(message + "\n" +  tmsp);
        styledString.setSpan(new StyleSpan(Typeface.NORMAL), 0, message.length()-1, 0);
        styledString.setSpan(new StyleSpan(Typeface.ITALIC), message.length(), tmsp.length()+message.length()-1, 0);
        styledString.setSpan(new ForegroundColorSpan(Color.BLUE), message.length(), tmsp.length()+message.length(), 0);

        chatText = row.findViewById(R.id.msg);
        chatText.setText(chatMessage.getAuthor() + ": " + styledString);

        return row;
    }
}

