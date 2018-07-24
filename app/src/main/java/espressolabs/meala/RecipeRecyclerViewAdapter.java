package espressolabs.meala;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.List;

import espressolabs.meala.RecipeListFragment.OnRecipeClickListener;
import espressolabs.meala.model.RecipeItem;

import static espressolabs.meala.utils.Constants.FAVORITES;
import static espressolabs.meala.utils.Constants.SHORTLIST;


public class RecipeRecyclerViewAdapter extends RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder> {

    private final List<RecipeItem> mValues;
    private final OnRecipeClickListener mListener;
    private final int mColumns;
    private final RequestManager glide;

    private final MyAdapterListener onClickListener;

    public interface MyAdapterListener {
        void buttonOnClick(View v, RecipeItem item, String list_type);
    }

    public RecipeRecyclerViewAdapter(List<RecipeItem> items, RecipeListFragment.OnRecipeClickListener listener, MyAdapterListener clickListener, RequestManager glide, int columnCount) {
        mValues = items;
        mListener = listener;
        onClickListener = clickListener;
        mColumns = columnCount;
        this.glide = glide;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int recipeLayout = mColumns == 1 ? R.layout.recipe_card : R.layout.recipe_tile;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(recipeLayout, parent, false);
        return new ViewHolder(view);
    }

    public void updateRecipeList(List<RecipeItem> newlist) {
        mValues.clear();
        mValues.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        if (mColumns <= 1) {
            holder.mText.setText(mValues.get(position).getSource());
        }

        holder.mTitle.setText(String.valueOf(mValues.get(position).getTitle()));

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });

        glide.load(mValues.get(position).getImage_url()).into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitle;
        final TextView mText;
        final ImageView mImage;
        RecipeItem mItem;

        ViewHolder(View itemView) {
            super(itemView);
            AppCompatImageButton fav_button = itemView.findViewById(R.id.favorite_button);
            AppCompatImageButton shortlist_button = itemView.findViewById(R.id.shortlist_button);
            AppCompatImageButton plan_button = itemView.findViewById(R.id.planner_button);

            if (fav_button != null) {
                fav_button.setOnClickListener(v -> onClickListener.buttonOnClick(v, mItem, FAVORITES));
            }

            if (shortlist_button != null) {
                shortlist_button.setOnClickListener(v -> onClickListener.buttonOnClick(v, mItem, SHORTLIST));
            }

            mView = itemView;
            mTitle = itemView.findViewById(R.id.recipe_title);
            mText = itemView.findViewById(R.id.recipe_text);
            mImage = itemView.findViewById(R.id.recipe_image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }
    }
}
