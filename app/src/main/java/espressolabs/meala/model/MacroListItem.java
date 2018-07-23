package espressolabs.meala.model;
import com.google.firebase.database.DataSnapshot;

public class MacroListItem {
    public String measurement;
    public float value;

    public MacroListItem(String measurement, float value) {
        this.measurement = measurement;
        this.value = value;
    }

    // need to get data from database then convert it to StatisticListItem
    public static MacroListItem fromSnapshot(DataSnapshot snapshot) {
        MacroListItem item = snapshot.getValue(MacroListItem.class);
        item.measurement = snapshot.getKey();

        return item;
    }
}