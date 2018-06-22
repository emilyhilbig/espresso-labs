package espressolabs.meala.ui.interaction;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import espressolabs.meala.R;
import espressolabs.meala.model.MealListItem;

public class PlanningListAdapter extends RecyclerView.Adapter<PlanningListAdapter.ViewHolder> implements ItemAnimator.onAnimationEndListener {

    private static final String TAG = "PlanningListAdapter";

    private final RecyclerView recyclerView;
    private final Context context;

    private ArrayList<MealListItem> data = new ArrayList<>();

    private Set<String> expandedItemKeys = new HashSet<>();

    public PlanningListAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public PlanningListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.meal_list_item;

        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        MealListItem item = data.get(position);

        // Reset values used by animation
        holder.itemView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        holder.resetBackgroundColor();
        holder.resetTextColor();
        holder.isExpanded = expandedItemKeys.contains(item.key);

        holder.nameTextView.setText(item.name);
        holder.createdByTextView.setText(context.getString(R.string.shopping_list_item_created_by, item.createdBy));

        holder.descriptionTextView.setVisibility((item.description.length() > 0) ? View.VISIBLE : View.GONE);
        holder.descriptionTextView.setText(item.description);

        holder.data = item;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onChangeEnd(final RecyclerView.ViewHolder newHolder) {
        int index = newHolder.getAdapterPosition();

        Log.v(TAG, "onChangeEnd index=" + index);

        if (index >= 0 && index <= data.size() - 1) {
            ViewHolder vh = (ViewHolder) newHolder;

            vh.isSwiped = false;
            expandedItemKeys.remove(vh.data.key);

            data.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void addItem(MealListItem item) {
        if (item.isActive()) {
            data.add(item);
            notifyItemInserted(data.indexOf(item));
        } else {
            Log.v(TAG, "addItem not active");
        }
    }

    public void setItems(Collection<MealListItem> items) {
        data.clear();
        data.addAll(items);

        // Urgent first, createdAt descend
        data.sort(Comparator.comparing(MealListItem::getMeal));

        notifyItemRangeInserted(0, data.size());
    }

    public void updateItem(MealListItem item) {
        int index = data.indexOf(item);
        Log.v(TAG, "updateItem index=" + index);

        if (index != -1) {
            data.set(index, item);
            notifyItemChanged(index);
        }
    }

    public void removeItem(MealListItem item) {
        int index = data.indexOf(item);
        Log.v(TAG, "removeItem index=" + index);

        if (index != -1) {
            data.remove(index);
            notifyItemRemoved(index);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView[] textViews;
        public final int[] defaultTextColors;
        public final int defaultBackgroundColor;

        public TextView mealTextView;
        public TextView nameTextView;
        public TextView createdByTextView;
        public TextView descriptionTextView;

        public MealListItem data;

        public boolean isSwiped;
        public boolean isExpanded = false;

        public ViewHolder(View vg) {
            super(vg);

            nameTextView = vg.findViewById(R.id.meal_list_item_name);
            createdByTextView = vg.findViewById(R.id.meal_list_item_created_by);
            descriptionTextView = vg.findViewById(R.id.meal_list_item_description);

            textViews = new TextView[]{
                    nameTextView,
                    createdByTextView,
                    descriptionTextView,
            };

            defaultTextColors = new int[textViews.length];
            for (int i = 0; i < defaultTextColors.length; i++) {
                defaultTextColors[i] = textViews[i].getCurrentTextColor();
            }

            defaultBackgroundColor = ((ColorDrawable) itemView.getBackground()).getColor();
        }

        public void resetTextColor() {
            for (int i = 0; i < defaultTextColors.length; i++) {
                textViews[i].setTextColor(defaultTextColors[i]);
            }
        }

        public void resetBackgroundColor() {
            itemView.setBackgroundColor(defaultBackgroundColor);
        }

    }

}
