package ch.ethz.inf.vs.project.forstesa.wiewowas.Location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import ch.ethz.inf.vs.project.forstesa.wiewowas.R;

/**
 * Created by Christian on 01.12.17.
 * Class to handle the Location part and get coordinates.
 */

public class UserLocation {

    public static interface LocationCallback {
        public void onNewLocationAvailable(Location location);
    }

    public static void requestLocation(final Context context, final LocationCallback callback) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                                                        == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestSingleUpdate(criteria, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        callback.onNewLocationAvailable(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }, null);
            }

        }
        else {
            Toast toast = Toast.makeText(context, R.string.GPS_not_available, Toast.LENGTH_LONG);
            toast.show();
        }

    }



    public static class GPSCoordinates {
        private double longitude;
        private double latitude;

        public GPSCoordinates(double latitude, double longitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public double[] getCoordinates() {
            double[] xy = new double[2];
            xy[0] = this.longitude;
            xy[1] = this.latitude;

            return xy;
        }

        public double getLongitude() {
            return this.longitude;
        }

        public double getLatitude() {
            return this.latitude;
        }

        private Location coor2loc() {
            Location res = new Location("result");
            res.setLatitude(this.latitude);
            res.setLongitude(this.longitude);
            return res;
        }

        public float distance(GPSCoordinates other) {

            float[] result = new float[3];
            Location.distanceBetween(this.getLongitude(), this.getLatitude(),
                    other.getLongitude(), other.getLatitude(), result);

            return result[0];

        }
    }
}
