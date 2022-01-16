package ch.ethz.inf.vs.project.forstesa.wiewowas.database;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ch.ethz.inf.vs.project.forstesa.wiewowas.R;

import static android.view.View.VISIBLE;

/**
 * Created by DellXPS_Kev on 15.11.2017.
 */

public class ChatDescriptionAdapter extends ArrayAdapter<ChatDescription> {

    private ArrayList<ChatDescription> oldgroups;
    private final ThreadLocal<ArrayList<ChatDescription>> groups = new ThreadLocal<>();
    private int layoutResourceId;
    private Context context;

    public ChatDescriptionAdapter(@NonNull Context context, int layoutResourceId, @NonNull List<ChatDescription> chatDescriptions) {
        super(context, layoutResourceId, chatDescriptions);
        this.groups.set((ArrayList<ChatDescription>) chatDescriptions);
        this.layoutResourceId = layoutResourceId;
        this.context = context;

        oldgroups = new ArrayList<>();
    }

    /**
     *
     * @param tags
     * Filter groups which contain at least one tag
     */
    public void filter(String[] tags){

        for(Iterator<ChatDescription> it = groups.get().iterator(); it.hasNext();){
            ChatDescription g = it.next();
            if(!g.hasTag(tags)){
                oldgroups.add(g);
                it.remove();
            }
        }
        notifyDataSetChanged();
    }

    public boolean contains(ChatDescription chatDescription){
        return groups.get().contains(chatDescription);
    }

    public void reset(){
        groups.get().addAll(oldgroups);
        oldgroups.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        GroupHolder holder = null;

        if(row == null){
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent,false);

            holder = new GroupHolder();
            holder.imgTickMark = (ImageView)row.findViewById(R.id.tickmark);
            holder.txtTags = (TextView) row.findViewById(R.id.tags);
            holder.txtTitle = (TextView) row.findViewById(R.id.groupname);

            row.setTag(holder);
        }else{
            holder = (GroupHolder)row.getTag();

        }

        ChatDescription chatDescription = groups.get().get(position);
        /**
         * Check if chatDescription is already joined and set the tickmarked if joined
         */
        holder.txtTitle.setText(chatDescription.getChatName());
        StringBuilder stringBuilder = new StringBuilder();
        for(String s :chatDescription.getTags()){
            if(!s.isEmpty() && !s.equals("") && !s.equals(" "))
                stringBuilder.append("#"+s+" ");
        }

        holder.txtTags.setText(stringBuilder);
        holder.imgTickMark.setVisibility((chatDescription.isJoined()) ? VISIBLE : View.INVISIBLE);


        return row;
    }


    static class GroupHolder
    {
        ImageView imgTickMark;
        TextView txtTitle;
        TextView txtTags;
    }
}
