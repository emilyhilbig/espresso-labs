package espressolabs.meala.model;
import com.google.firebase.database.DataSnapshot;

public class MacroListItem {
    public String name;
    public float value;
    public boolean isSet;

    public MacroListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(RecipeItem.class)
    }

    public MacroListItem(String name, float value, boolean isSet) {
        this.name = name;
        this.value = value;
        this.isSet = isSet;
    }

    // need to get data from database then convert it to StatisticListItem
    public static MacroListItem fromSnapshot(DataSnapshot snapshot) {
        MacroListItem item = snapshot.getValue(MacroListItem.class);
        item.name = snapshot.getKey();

        return item;
    }
}