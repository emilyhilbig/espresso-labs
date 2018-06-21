package espressolabs.meala.utils;

import java.util.List;
import java.util.Random;

import espressolabs.meala.model.RecipeContent;

public class ShuffleList {
    public static void shuffleList(List<RecipeContent.Recipe> a) {
        int n = a.size();
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(a, i, change);
        }
    }

    private static void swap(List<RecipeContent.Recipe> a, int i, int change) {
        RecipeContent.Recipe helper = a.get(i);
        a.set(i, a.get(change));
        a.set(change, helper);
    }
}