package com.iskcon.bvks.ui.filter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.iskcon.bvks.R;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    public static final String FILTER_INTENT_EXTRA = "filterExtra";
    public static final String FAVORITE_FILTER_INTENT_EXTRA = "filterExtra";
    private static String callingFragment;

    private static FilterHelper sFilterHelper;
    private static RecyclerView.Adapter sAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_list_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        callingFragment = getIntent().getStringExtra("fragNameForFilter");

        View recyclerView = findViewById(R.id.item_list);
        sAdapter = new SimpleItemRecyclerViewAdapter(this, Arrays.asList(Filter.values()));
        ((RecyclerView) recyclerView).setAdapter(sAdapter);

        TextView applyTextView = findViewById(R.id.applyTextView);
        applyTextView.setOnClickListener(v -> {
            sFilterHelper.saveFilteredItems();
            finish();
        });

        sFilterHelper = new FilterHelper(this, callingFragment);

        showFilterDetailFragment(this, Filter.LANGUAGES);
    }

    @Override
    public void finish() {

        Intent data = new Intent();
        if(callingFragment == "FavoriteListFragment"){
            data.putExtra(FAVORITE_FILTER_INTENT_EXTRA, sFilterHelper.getSelectedItemMap());
        }else {
            data.putExtra(FILTER_INTENT_EXTRA, sFilterHelper.getSelectedItemMap());
        }
        setResult(RESULT_OK, data);
        sFilterHelper.clear(false);
        super.finish();
    }

    private static void showFilterDetailFragment(@NonNull FilterActivity activity,
                                                 @NonNull Filter item) {
        Bundle arguments = new Bundle();
        if(callingFragment == "FavoriteListFragment")
            arguments.putSerializable(FilterItemDetailFragment.FAV_ARG_ITEM_ID, item);
        else arguments.putSerializable(FilterItemDetailFragment.ARG_ITEM_ID, item);
        FilterItemDetailFragment fragment = new FilterItemDetailFragment();
        fragment.setArguments(arguments);
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, fragment)
                .commit();
    }

    private static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final FilterActivity mParentActivity;
        private final List<Filter> mValues;
        private Filter mSelectedFilter;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedFilter = (Filter) view.getTag();
                notifyDataSetChanged();
                showFilterDetailFragment(mParentActivity, mSelectedFilter);
            }
        };

        SimpleItemRecyclerViewAdapter(FilterActivity parent, List<Filter> items) {
            mValues = items;
            mParentActivity = parent;
            mSelectedFilter = Filter.LANGUAGES;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Filter item = mValues.get(position);
            holder.mIdView.setText(item.getTitle());
            holder.itemView.setBackgroundColor(0);
            holder.itemView.setTag(item);
            holder.itemView.setOnClickListener(mOnClickListener);
            if (item == mSelectedFilter) {
                holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimaryLight, null));
            } else {
                holder.itemView.setBackgroundColor(0);
            }
            List<String> filterContent = sFilterHelper.getSelectedItemMap().get(item.getTitle());
            if (filterContent != null && filterContent.size() > 0) {
                holder.mFilterNumberView.setText(String.valueOf(filterContent.size()));
                holder.mFilterNumberView.setVisibility(View.VISIBLE);
            } else {
                holder.mFilterNumberView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mFilterNumberView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mFilterNumberView = view.findViewById(R.id.filter_number);
            }
        }
    }

    public static class FilterItemDetailFragment extends ListFragment
            implements LoaderManager.LoaderCallbacks<List<String>> {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        public static final String ARG_ITEM_ID = "item_id";
        public static final String FAV_ARG_ITEM_ID = "fav_item_id";


        private static Filter sFilter;

        // This is the Adapter being used to display the list's data.
        private AppListAdapter mAdapter;

        /**
         * Mandatory empty constructor for the fragment manager to instantiate the
         * fragment (e.g. upon screen orientation changes).
         */
        public FilterItemDetailFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments().containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                sFilter = (Filter) getArguments().getSerializable(ARG_ITEM_ID);
            }
            if (getArguments().containsKey(FAV_ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                sFilter = (Filter) getArguments().getSerializable(FAV_ARG_ITEM_ID);
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No filter to select");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new AppListAdapter(getActivity());
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.filter_menu, menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.clear:
                    sFilterHelper.clear(true);
                    sAdapter.notifyDataSetChanged();
                    mAdapter.notifyDataSetChanged();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public Loader<List<String>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader with no arguments, so it is simple.
            return new AppListLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
            // Set the new data in the adapter.
            mAdapter.setData(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<String>> loader) {
            // Clear the data in the adapter.
            mAdapter.setData(null);
        }

        private static class AppListAdapter extends ArrayAdapter<String> implements View.OnClickListener {
            private final LayoutInflater mInflater;

            public AppListAdapter(Context context) {
                super(context, android.R.layout.simple_list_item_1);
                mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            public void setData(List<String> data) {
                clear();
                if (data != null) {
                    addAll(data);
                }
            }

            /**
             * Populate new items in the list.
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;

                if (convertView == null) {
                    view = mInflater.inflate(R.layout.filter_item_detail, parent, false);
                } else {
                    view = convertView;
                }

                CheckBox checkBox = view.findViewById(R.id.item_detail);
                String text = getItem(position);
                if (sFilter == Filter.MONTH) {
                    checkBox.setText(new DateFormatSymbols().getMonths()[Integer.valueOf(text)]);
                } else {
                    checkBox.setText(text);
                }
                checkBox.setTag(text);
                checkBox.setChecked(sFilterHelper.isSelected(sFilter, text));
                checkBox.setOnClickListener(this);

                return view;
            }

            @Override
            public void onClick(View v) {
                // Is the view now checked?
                boolean checked = ((CheckBox) v).isChecked();

                // Check which checkbox was clicked
                switch (v.getId()) {
                    case R.id.item_detail:
                        if (checked) {
                            sFilterHelper.filterDataSelected(sFilter, (String) v.getTag());
                        } else {
                            sFilterHelper.filterDataUnselected(sFilter, (String) v.getTag());
                        }
                        sAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * A custom Loader that loads all of the installed applications.
         */
        private static class AppListLoader extends AsyncTaskLoader<List<String>> {
            List<String> mApps;

            public AppListLoader(Context context) {
                super(context);
            }

            /**
             * This is where the bulk of our work is done.  This function is
             * called in a background thread and should generate a new set of
             * data to be published by the loader.
             */
            @Override
            public List<String> loadInBackground() {
                List<String> entries = sFilterHelper.readFilterData(sFilter);
                sFilterHelper.sortFilterData(sFilter, entries);
                // Done!
                return entries;
            }

            /**
             * Called when there is new data to deliver to the client.  The
             * super class will take care of delivering it; the implementation
             * here just adds a little more logic.
             */
            @Override
            public void deliverResult(List<String> apps) {
                mApps = apps;

                if (isStarted()) {
                    // If the Loader is currently started, we can immediately
                    // deliver its results.
                    super.deliverResult(apps);
                }
            }

            /**
             * Handles a request to start the Loader.
             */
            @Override
            protected void onStartLoading() {
                if (mApps != null) {
                    // If we currently have a result available, deliver it
                    // immediately.
                    deliverResult(mApps);
                }
                if (takeContentChanged() || mApps == null) {
                    // If the data has changed since the last time it was loaded
                    // or is not currently available, start a load.
                    forceLoad();
                }
            }

            /**
             * Handles a request to stop the Loader.
             */
            @Override
            protected void onStopLoading() {
                // Attempt to cancel the current load task if possible.
                cancelLoad();
            }

            /**
             * Handles a request to completely reset the Loader.
             */
            @Override
            protected void onReset() {
                super.onReset();

                // Ensure the loader is stopped
                onStopLoading();

                // At this point we can release the resources associated with 'apps'
                // if needed.
                if (mApps != null) {
                    mApps = null;
                }
            }
        }
    }
}
