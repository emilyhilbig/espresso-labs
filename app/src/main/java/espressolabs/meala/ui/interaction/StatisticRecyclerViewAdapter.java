package espressolabs.meala.ui.interaction;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.Collection;

import az.plainpie.PieView;
import espressolabs.meala.R;
import espressolabs.meala.StatisticFragment;
import espressolabs.meala.model.ShoppingListItem;
import espressolabs.meala.model.StatisticListItem;

public class StatisticRecyclerViewAdapter extends RecyclerView.Adapter<StatisticRecyclerViewAdapter.ViewHolder> implements ItemAnimator.onAnimationEndListener{

    //private final List<RecipeContent.Recipe> mValues;
    private static final String TAG = "StatisticRecyclerViewAdapter";
    private final StatisticFragment.OnStatisticClickListener mListener;
    private final int mColumns;
    private final RequestManager glide;
    private ArrayList<StatisticListItem> data = new ArrayList<>();

    public StatisticRecyclerViewAdapter(StatisticFragment.OnStatisticClickListener listener, RequestManager glide, int columnCount) { //List<RecipeContent.Recipe> items,
        //mValues = items;
        mListener = listener;
        mColumns = columnCount;
        this.glide = glide;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //int recipeLayout = mColumns == 1 ? R.layout.recipe_card : R.layout.recipe_tile;
        //View view = LayoutInflater.from(parent.getContext())
        //        .inflate(recipeLayout, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_card, parent, false);
        ((PieView) view.findViewById(R.id.pieView)).setPercentage(50);

        return new ViewHolder(view);
    }

    /*
    public void updateStatisticList(List<RecipeContent.Recipe> newlist) {
        mValues.clear();
        mValues.addAll(newlist);
        this.notifyDataSetChanged();
    }*/

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        StatisticListItem item = data.get(position);
        holder.data = item;
        /*holder.mItem = mValues.get(position);

        if (mColumns <= 1) {
            holder.mText.setText(mValues.get(position).details);
        }

        holder.mTitle.setText(String.valueOf(mValues.get(position).title));
*/
        holder.pieView.setPercentage(item.value);
        holder.pieView.setInnerText(item.name);

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
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public StatisticListItem data;

        public PieView pieView;

        public ViewHolder(View itemView) {
            super(itemView);

            pieView = itemView.findViewById(R.id.pieView);
        };

        @Override
        public void onClick(View view) {};
        /*public final View mView;
        public final TextView mTitle;
        public final TextView mText;
        public final ImageView mImage;
        public RecipeContent.Recipe mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            AppCompatImageButton fav_button = itemView.findViewById(R.id.favorite_button);
            AppCompatImageButton sl_button = itemView.findViewById(R.id.shortlist_button);
            AppCompatImageButton pln_button = itemView.findViewById(R.id.confirm_button);
            if (fav_button != null) {fav_button.setOnClickListener(this);}
            if (sl_button != null) {sl_button.setOnClickListener(this);}
            if (pln_button != null) {pln_button.setOnClickListener(this);}

            mView = itemView;
            mTitle = itemView.findViewById(R.id.recipe_title);
            mText = itemView.findViewById(R.id.recipe_text);
            mImage = itemView.findViewById(R.id.recipe_image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();

            String verb = "Added to ";
            switch (id) {
                case R.id.favorite_button:
                    // button event
                    Toast.makeText(view.getContext(), verb + "favorites!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.shortlist_button:
                    Toast.makeText(view.getContext(), verb + "shortlist!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.confirm_button:
                    Snackbar.make(view, verb + "planner!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    break;

            }
        }*/
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

    public void setItems(Collection<StatisticListItem> items) {
        data.clear();
        data.addAll(items);

        notifyItemRangeInserted(0, data.size());
    }
}
