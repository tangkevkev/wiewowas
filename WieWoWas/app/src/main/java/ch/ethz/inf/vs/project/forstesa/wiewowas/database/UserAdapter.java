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
import java.util.List;

import ch.ethz.inf.vs.project.forstesa.wiewowas.R;

/**
 * Created by DellXPS_Kev on 22.11.2017.
 */

public class UserAdapter extends ArrayAdapter<User> {

        private final ThreadLocal<ArrayList<User>>  users = new ThreadLocal<>();
        private int layoutResourceId;
        private Context context;

    public UserAdapter(@NonNull Context context, int layoutResourceId,@NonNull List<User> users) {
        super(context, layoutResourceId, users);
        this.users.set((ArrayList<User>) users);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        UserHolder holder = null;

        if(row == null){
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent,false);

            holder = new UserHolder();
            // holder.imgArrow = (ImageView)row.findViewById(R.id.arrow);
            holder.txtUserAndInfo = (TextView) row.findViewById(R.id.usernameAndinfo);
            holder.txtLocation = (TextView) row.findViewById(R.id.location);

            row.setTag(holder);
        }else{
            holder = (UserHolder)row.getTag();
        }

        User user= users.get().get(position);
        String nameDist = (user.getName() + ": " + user.getDistanceTo() + "m");
        holder.txtLocation.setText(user.getLocationName());
        holder.txtUserAndInfo.setText(nameDist);

        return row;
    }


    static class UserHolder
    {
        // ImageView imgArrow;
        TextView txtUserAndInfo;
        TextView txtLocation;
    }
}


