package espressolabs.meala;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;

import java.util.List;

import espressolabs.meala.RecipeListFragment.OnRecipeClickListener;
import espressolabs.meala.model.RecipeContent.Recipe;


public class RecipeRecyclerViewAdapter extends RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder> {

    private final List<Recipe> mValues;
    private final OnRecipeClickListener mListener;
    private final int mColumns;
    private final RequestManager glide;

    public RecipeRecyclerViewAdapter(List<Recipe> items, RecipeListFragment.OnRecipeClickListener listener, RequestManager glide, int columnCount) {
        mValues = items;
        mListener = listener;
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

    public void updateRecipeList(List<Recipe> newlist) {
        mValues.clear();
        mValues.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        if (mColumns <= 1) {
            holder.mText.setText(mValues.get(position).details);
        }

        holder.mTitle.setText(String.valueOf(mValues.get(position).title));

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });

        glide.load(mValues.get(position).image_url).into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView mTitle;
        public final TextView mText;
        public final ImageView mImage;
        public Recipe mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            AppCompatImageButton fav_button = itemView.findViewById(R.id.favorite_button);
            AppCompatImageButton sl_button = itemView.findViewById(R.id.shortlist_button);
            AppCompatImageButton pln_button = itemView.findViewById(R.id.planner_button);
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
                case R.id.planner_button:
                    Snackbar.make(view, verb + "planner!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    break;

            }
        }
    }
}
