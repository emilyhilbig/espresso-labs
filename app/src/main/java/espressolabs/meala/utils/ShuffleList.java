package espressolabs.meala.utils;

import java.util.List;
import java.util.Random;

import espressolabs.meala.model.RecipeItem;

public class ShuffleList {
    public static void shuffleList(List<RecipeItem> a) {
        int n = a.size();
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(a, i, change);
        }
    }

    private static void swap(List<RecipeItem> a, int i, int change) {
        RecipeItem helper = a.get(i);
        a.set(i, a.get(change));
        a.set(change, helper);
    }
}