package in.hedera.reku.sensorscanner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.hedera.reku.sensorscanner.data.BeaconRecyAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private BeaconRecyAdapter adapter;
    private TextView emptylayout;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = view.findViewById(R.id.luxbeacon_recycler_view);
        emptylayout = view.findViewById(R.id.empty_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new BeaconRecyAdapter(((MainActivity) getActivity()).getCollection(), emptylayout);
        mRecyclerView.setAdapter(adapter);
        return view;
    }

    public void updateRecycler(){
        adapter.updateBeaconData(((MainActivity) getActivity()).getCollection());
    }

    public void clearRecycler() {
        adapter.cleardata();
    }
}
