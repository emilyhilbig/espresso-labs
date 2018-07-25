package espressolabs.meala.ui.interaction;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Switch;

import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.Collection;

import espressolabs.meala.R;
import espressolabs.meala.ProfileFragment;
import espressolabs.meala.model.MacroListItem;

public class ProfileViewAdapter extends RecyclerView.Adapter<ProfileViewAdapter.ViewHolder> implements ItemAnimator.onAnimationEndListener{

    //private final List<RecipeContent.Recipe> mValues;
    private static final String TAG = "ProfileViewAdapter";
    private final ProfileFragment.OnStatisticClickListener mListener;
    private final int mColumns;
    private final RequestManager glide;
    private ArrayList<MacroListItem> data = new ArrayList<>();

    private final ProfileViewAdapter.MyAdapterListener onChangeListener;

    public interface MyAdapterListener {
        void OnChangeListener(View v, MacroListItem item, EditText goalText, boolean hasFocus);
    }

    public ProfileViewAdapter(ProfileFragment.OnStatisticClickListener listener, RequestManager glide, ProfileViewAdapter.MyAdapterListener clickListener, int columnCount) { //List<RecipeContent.Recipe> items,
        //mValues = items;
        mListener = listener;
        mColumns = columnCount;
        onChangeListener = clickListener;
        this.glide = glide;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //int recipeLayout = mColumns == 1 ? R.layout.recipe_card : R.layout.recipe_tile;
        //View view = LayoutInflater.from(parent.getContext())
        //        .inflate(recipeLayout, parent, false);
        ViewGroup vg = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list_item, parent, false);
        //((PieView) view.findViewById(R.id.pieView)).setPercentage(50);

        return new ViewHolder(vg);
    }

    /*
    public void updateStatisticList(List<RecipeContent.Recipe> newlist) {
        mValues.clear();
        mValues.addAll(newlist);
        this.notifyDataSetChanged();
    }*/

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        MacroListItem item = data.get(position);
        holder.goalText.setText(String.valueOf(item.value));
        holder.nameText.setText(item.name);
        holder.settingSwitch.setChecked(item.isSet);

        holder.goalText.setOnFocusChangeListener((v, hasFocus) -> onChangeListener.OnChangeListener(v, item, holder.goalText, hasFocus)); // maybe only need to listen to un-onFocusChange

        /*
        if (mColumns <= 1) {
            holder.mText.setText(mValues.get(position).details);
        }

        holder.mTitle.setText(String.valueOf(mValues.get(position).title));
*/
        holder.itemView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                //mListener.onListFragmentInteraction(holder.mItem);
            }
        });

        //glide.load(mValues.get(position).image_url).into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        return data.size();
        //return 2;
    }

    @Override
    public void onChangeEnd(final RecyclerView.ViewHolder newHolder) {
        int index = newHolder.getAdapterPosition();

        Log.v(TAG, "onChangeEnd index=" + index);
/*
        if (index >= 0 && index <= data.size() - 1) {
            ShoppingListAdapter.ViewHolder vh = (ShoppingListAdapter.ViewHolder) newHolder;

            vh.isSwiped = false;
            expandedItemKeys.remove(vh.data.key);

            data.remove(index);
            notifyItemRemoved(index);
        }*/
    }

    public void setItems(Collection<MacroListItem> items) {
        data.clear();
        data.addAll(items);

        notifyItemRangeInserted(0, data.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //public final int[] defaultTextColors;
        public final int defaultBackgroundColor;

        public TextView nameText;
        public EditText goalText;
        public Switch settingSwitch;

        public MacroListItem data;

        public boolean isExpanded = false;

        public ViewHolder(ViewGroup vg) {
            super(vg);

            goalText = vg.findViewById(R.id.setDailyGoal);
            nameText = vg.findViewById(R.id.txtDailyGoal);
            settingSwitch = vg.findViewById(R.id.setSwitch);

            /*defaultTextColors = new int[statisticPieView.length];
            for (int i = 0; i < defaultTextColors.length; i++) {
                defaultTextColors[i] = textViews[i].getCurrentTextColor();
            }*/

            defaultBackgroundColor = ((ColorDrawable) itemView.getBackground()).getColor();
        }

        /*public void resetTextColor() {
            for (int i = 0; i < defaultTextColors.length; i++) {
                textViews[i].setTextColor(defaultTextColors[i]);
            }
        }*/

        public void resetBackgroundColor() {
            itemView.setBackgroundColor(defaultBackgroundColor);
        }

    }
}
