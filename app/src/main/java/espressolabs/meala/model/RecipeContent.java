package espressolabs.meala.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample title for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class RecipeContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Recipe> ITEMS = new ArrayList<Recipe>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Recipe> ITEM_MAP = new HashMap<String, Recipe>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
        }
    }

// --Commented out by Inspection START (2018-06-21, 04:14):
//    private static void addItem(Recipe item) {
//        ITEMS.add(item);
//        ITEM_MAP.put(String.valueOf(item.id), item);
//    }
// --Commented out by Inspection STOP (2018-06-21, 04:14)

// --Commented out by Inspection START (2018-06-21, 04:14):
//    private static Recipe createDummyItem(int position) {
//        return new Recipe(position, "Item " + position, makeDetails(position), "http://via.placeholder.com/300.png", "https://example.com");
//    }
// --Commented out by Inspection STOP (2018-06-21, 04:14)

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class Recipe {
        public final int id;
        public final String title;
        public final String details;
        public final String image_url;
        public final String link;

        public Recipe(int id, String title, String details, String image_url, String link) {
            this.id = id;
            this.title = title;
            this.details = details;
            this.image_url = image_url;
            this.link = link;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
