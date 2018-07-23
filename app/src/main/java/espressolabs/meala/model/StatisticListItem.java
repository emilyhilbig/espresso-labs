package espressolabs.meala.model;

import com.google.firebase.database.DataSnapshot;

public class StatisticListItem {
    public String measurement;
    public float value;
    public String name;

    public StatisticListItem(String measurement, String name, float value) {
        this.measurement = measurement;
        this.value = value;
        this.name = name;
    }

    // need to get data from database then convert it to StatisticListItem
    public static StatisticListItem fromSnapshot(DataSnapshot snapshot) {
        StatisticListItem item = new StatisticListItem("%", "Calories",90);//snapshot.getValue(StatisticListItem.class);
        //item.measurement = snapshot.getKey();

        return item;
    }
}
