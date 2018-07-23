package espressolabs.meala.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

@IgnoreExtraProperties
public class RecipeItem {

    @Exclude
    public String key;

    public Status status;
    public long createdAt;
    public long interactedAt;

    @Exclude
    public JSONObject data;

    public String jsondata;

    private String getData(String label) {
        if (data != null) {
            try {
                return data.getString(label);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            return "";
        }
    }

    public String getTitle() {
        return getData("label");
    }

    public String getImage_url() {
        return getData("image");
    }

    public String getLink() {
        return getData("url");
    }

    public String getSource() {
        return getData("source");
    }

    @SuppressWarnings("unused")
    public RecipeItem() {
        // Default constructor required for calls to DataSnapshot.getValue(RecipeItem.class)
    }


    public RecipeItem(JSONObject data) {
        this.data = data;
        this.jsondata = data.toString();
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
        this.interactedAt = -1;

        String uri = getData("uri");
        // e.g. http://www.edamam.com/ontologies/edamam.owl#recipe_190c92e146d74721c550305c1d1c524c
        int hash_idx = uri.lastIndexOf('#');
        this.key = uri.substring(hash_idx + 8);
    }

    public static RecipeItem fromSnapshot(DataSnapshot snapshot) {
        RecipeItem item = snapshot.getValue(RecipeItem.class);
        item.key = snapshot.getKey();
        try {
            item.data = new JSONObject(item.jsondata);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return item;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof RecipeItem && key.equals(((RecipeItem) obj).key);
    }

    @Override
    public String toString() {
        return "RecipeItem{" +
                "key='" + key + '\'' +
                ", name='" + getTitle() + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    public void archive() {
        status = Status.ARCHIVED;
    }

    public void delete() {
        status = Status.DELETED;
    }

    @Exclude
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public enum Status {
        ACTIVE, ARCHIVED, DELETED
    }

}
