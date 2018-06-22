package espressolabs.meala.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.ArrayList;

@IgnoreExtraProperties
public class PlanListItem {

    @Exclude
    public String key;

    public String createdBy;
    public Date date;
    public Status status;
    public ArrayList<MealListItem> meals;
    public long createdAt;

    @SuppressWarnings("unused")
    public PlanListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingListItem.class)
    }

    public PlanListItem(String createdBy) {
        this.createdBy = createdBy;
        this.date = date;
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
    }

    public static PlanListItem fromSnapshot(DataSnapshot snapshot) {
        PlanListItem item = snapshot.getValue(PlanListItem.class);
        item.key = snapshot.getKey();

        return item;
    }

    @Override
    public String toString() {
        return "PlanListItem{" +
                "key='" + key + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public void delete() {
        status = Status.DELETED;
    }

    public void addMeal(MealListItem m) {
        this.meals.add(m);
    }

    public Date getDate() {
        return this.date;
    }

    @Exclude
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public enum Status {
        ACTIVE, DELETED
    }
}
