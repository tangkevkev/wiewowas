package ch.ethz.inf.vs.project.forstesa.wiewowas.database;


/**
 * Created by DellXPS_Kev on 22.11.2017.
 */

public class User {

    private String name;
    private String locationName;
    private double distanceToUser;

    public User(String name, double distance, String locationName) {
        this.name = name;
        this.distanceToUser = distance;
        this.locationName = locationName;
    }

    public String getName(){
        return name;
    }

    public String getLocationName(){
        return locationName;
    }

    public int getDistanceTo() {
        return (int) (distanceToUser + 0.5);
    }
}
