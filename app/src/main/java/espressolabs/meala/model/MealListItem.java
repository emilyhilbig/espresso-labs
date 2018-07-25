package espressolabs.meala.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class MealListItem {

    @Exclude
    public String key;

    public String createdBy;
    public String name;
    public String description;
    public Status status;
    public Meal meal;
    public long createdAt;

    @SuppressWarnings("unused")
    public MealListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingListItem.class)
    }

    public MealListItem(String createdBy, String name, String description, Meal meal) {
        this.createdBy = createdBy;
        this.name = name;
        this.description = description;
        this.meal = meal;
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
    }

    public static MealListItem fromSnapshot(DataSnapshot snapshot) {
        MealListItem item = snapshot.getValue(MealListItem.class);
        item.key = snapshot.getKey();

        return item;
    }

    @Override
    public String toString() {
        return "ShoppingListItem{" +
                "key='" + key + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public void delete() {
        status = Status.DELETED;
    }

    public Meal getMeal(){
        return meal;
    }

    @Exclude
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public enum Status {
        ACTIVE, DELETED
    }

    public enum Meal {
        BREAKFAST,
        LUNCH,
        DINNER,
        SNACK
    }
}
