package io.github.deepbluecitizenservice.citizenservice.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedList;
import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.GraphDialog;
import io.github.deepbluecitizenservice.citizenservice.MainActivity;
import io.github.deepbluecitizenservice.citizenservice.MapsActivity;
import io.github.deepbluecitizenservice.citizenservice.R;
import io.github.deepbluecitizenservice.citizenservice.adapter.CommonRecyclerViewAdapter;
import io.github.deepbluecitizenservice.citizenservice.database.ProblemModel;
import io.github.deepbluecitizenservice.citizenservice.database.QueryModel;

public class AllViewFragment extends Fragment {
    private QueryModel queryModel;

    public AllViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_view, container, false);
        final CardView noInternetCard = (CardView) v.findViewById(R.id.no_connection_card);

        ((MainActivity)getActivity()).setupNoInternetCard(noInternetCard);
        ((MainActivity)getActivity()).checkInternetConnectivity(noInternetCard);


        queryModel = new QueryModel();

        List<ProblemModel> problemModelList = new LinkedList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            return v;
        }


        final DatabaseReference problemRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("problems");

        final DatabaseReference solutionRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("solutions");

        final DatabaseReference[] ref = {problemRef};

        RecyclerView rv = (RecyclerView) v.findViewById(R.id.all_recycle_view);

        final CommonRecyclerViewAdapter adapter = new CommonRecyclerViewAdapter(rv, getContext(), problemModelList, MainActivity.ALL_TAG);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(linearLayoutManager);
        rv.addItemDecoration(new QueryModel.SpacingDecoration(8));
        rv.setAdapter(adapter);

        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.all_view_swipe);

        queryModel.makeQuery(0- (System.currentTimeMillis()/1000), ref[0], adapter, refreshLayout);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(queryModel.allAdded &&
                        linearLayoutManager.getItemCount() <=
                                linearLayoutManager.findLastVisibleItemPosition() + QueryModel.OFFSET_VIEW){
                    queryModel.makeQuery(queryModel.lastSeen, ref[0], adapter, refreshLayout);
                }
            }
        });


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                queryModel.lastSeen = 0-(System.currentTimeMillis()/1000);
                ((MainActivity)getActivity()).checkInternetConnectivity(noInternetCard);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(refreshLayout.isRefreshing()) {
                            refreshLayout.setRefreshing(false);
                            Snackbar.make(refreshLayout, R.string.swipe_network_error,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }, 5000);
            }
        });

        Switch showSolutionSwitch = (Switch) v.findViewById(R.id.all_view_solution_switch);
        showSolutionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.clear();
                queryModel.lastSeen = 0-(System.currentTimeMillis()/1000);
                if (isChecked){
                    ref[0] = solutionRef;
                }
                else {
                    ref[0] = problemRef;
                }
            }
        });

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.all_view_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_all_view_map:
                startActivity(new Intent(getContext(), MapsActivity.class));
                break;
            case R.id.toolbar_all_view_chart:
                GraphDialog dialog = new GraphDialog(getContext(), getString(R.string.graph_all_stats_title), GraphDialog.ALL_PROBLEMS);
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
