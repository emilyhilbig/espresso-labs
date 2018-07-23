package espressolabs.meala.model;

import com.google.firebase.database.DataSnapshot;

public class StatisticListItem {
    public String measurement;
    public float value;

    public StatisticListItem(String measurement, float value) {
        this.measurement = measurement;
        this.value = value;
    }

    // need to get data from database then convert it to StatisticListItem
    public static StatisticListItem fromSnapshot(DataSnapshot snapshot) {
        StatisticListItem item = new StatisticListItem("%",90);//snapshot.getValue(StatisticListItem.class);
        //item.measurement = snapshot.getKey();

        return item;
    }
}
