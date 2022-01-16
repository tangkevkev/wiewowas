package ch.ethz.inf.vs.project.forstesa.wiewowas.Location;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import ch.ethz.inf.vs.project.forstesa.wiewowas.MainActivity;
import ch.ethz.inf.vs.project.forstesa.wiewowas.R;


public class PermissionDialogFragment extends DialogFragment {

    public static boolean finished = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.give_permission)
                .setTitle(R.string.permission_disabled)
                .setPositiveButton(R.string.grant_permission, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    }
                });

        return builder.create();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getActivity(), R.string.permission_disabled, Toast.LENGTH_SHORT).show();
                }

            }

        }
        return;
    }
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100;
}

