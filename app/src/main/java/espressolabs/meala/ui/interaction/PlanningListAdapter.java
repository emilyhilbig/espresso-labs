package espressolabs.meala.ui.interaction;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import espressolabs.meala.R;
import espressolabs.meala.model.MealListItem;

public class PlanningListAdapter extends RecyclerView.Adapter<PlanningListAdapter.ViewHolder> {

    private static final String TAG = "PlanningListAdapter";

    private List<MealListItem> data;

    private Set<String> expandedItemKeys = new HashSet<>();

    public PlanningListAdapter(List<MealListItem> data) {
        this.data = new ArrayList<>(data);
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
        MealListItem mData = data.get(position);

        holder.nameTextView.setText(mData.name);
        holder.descriptionTextView.setText(mData.description);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void addItem(MealListItem item) {
        data.add(item);

        notifyDataSetChanged();
        Log.v(TAG, data.toString());
        //notifyItemInserted(data.indexOf(item));
    }

    public void setItems(Collection<MealListItem> items) {
        int previousContentSize = data.size();
        data.clear();
        data.addAll(items);

        notifyItemRangeRemoved(0, previousContentSize);
        notifyItemRangeInserted(0, data.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView[] textViews;
        final TextView nameTextView;
        final TextView createdByTextView;
        final TextView descriptionTextView;

        public MealListItem data;

        ViewHolder(View vg) {
            super(vg);

            nameTextView = vg.findViewById(R.id.meal_list_item_name);
            createdByTextView = vg.findViewById(R.id.meal_list_item_created_by);
            descriptionTextView = vg.findViewById(R.id.meal_list_item_description);

            textViews = new TextView[]{
                    nameTextView,
                    createdByTextView,
                    descriptionTextView,
            };
        }

    }

}
