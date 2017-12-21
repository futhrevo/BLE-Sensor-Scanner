package in.hedera.reku.sensorscanner.data;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;

import in.hedera.reku.sensorscanner.R;

/**
 * Created by rakeshkalyankar on 17/12/17.
 */

public class BeaconRecyAdapter extends RecyclerView.Adapter<BeaconRecyAdapter.ViewHolder>{

    private ArrayList<LuxBeacon> values = new ArrayList<>();
    private TextView emptylayout;

    public BeaconRecyAdapter(Collection<Beacon> collection, TextView emptylayout) {
        this.emptylayout = emptylayout;
        if(collection == null) {
            return;
        }
        for (Beacon beacon : collection) {
            LuxBeacon luxBeacon = new LuxBeacon(beacon);
            values.add(luxBeacon);
        }
        if(values.isEmpty()){
            emptylayout.setText(R.string.empty_list);
            emptylayout.setVisibility(View.VISIBLE);
        }else{
            emptylayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.beacon_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LuxBeacon luxBeacon = values.get(position);
        holder.tv_beacon_lux.setText(Long.toString(luxBeacon.getLux()));
        holder.tv_beacon_mac.setText(luxBeacon.getBeacon().getBluetoothAddress());
        holder.tv_beacon_name.setText(luxBeacon.getBeacon().getBluetoothName());
        holder.tv_beacon_rssi.setText(String.valueOf(luxBeacon.getBeacon().getRssi()));
        holder.tv_beacon_temp.setText(Long.toString(luxBeacon.getTemperature()));
        holder.rtv_beacon_lastseen.setReferenceTime(luxBeacon.getAttime());
        if(luxBeacon.isActive()){
            holder.cardView.setBackgroundColor(Color.parseColor("#ffffff"));
        }else{
            holder.cardView.setBackgroundColor(Color.parseColor("#757575"));
        }

        new CountDownTimer(1000, 500){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                holder.cardView.setBackgroundColor(Color.parseColor("#757575"));
            }
        }.start();
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public void updateBeaconData(Collection<Beacon> collection){
        if(collection == null) {
            return;
        }
        int size = getItemCount();
        if(size == 0){
            for (Beacon beacon : collection) {
                LuxBeacon luxBeacon = new LuxBeacon(beacon);
                values.add(luxBeacon);
            }
        }else{
            for (Beacon beacon : collection) {
                String nmac = beacon.getBluetoothAddress();
                int index = -1;
                for(int i=0; i < size; i++){
                    LuxBeacon luxBeacon = values.get(i);
                    if(luxBeacon.getBeacon().getBluetoothAddress().equals(nmac)){
                        values.set(i, new LuxBeacon(beacon));
                        index = i;
                        break;
                    }
                }
                if(index < 0){
                    values.add(new LuxBeacon(beacon));
                }
            }
        }

        notifyDataSetChanged();
        if(values.isEmpty()){
            emptylayout.setText(R.string.empty_list);
            emptylayout.setVisibility(View.VISIBLE);
        }else{
            emptylayout.setVisibility(View.INVISIBLE);
        }
    }

    public void cleardata() {
        values.clear();
        notifyDataSetChanged();
        emptylayout.setText(R.string.empty_start);
        emptylayout.setVisibility(View.VISIBLE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView tv_beacon_name;
        public TextView tv_beacon_mac;
        public TextView tv_beacon_temp;
        public TextView tv_beacon_lux;
        public TextView tv_beacon_rssi;
        public RelativeTimeTextView rtv_beacon_lastseen;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_beacon_name = itemView.findViewById(R.id.beacon_name);
            tv_beacon_mac = itemView.findViewById(R.id.beacon_mac);
            tv_beacon_temp = itemView.findViewById(R.id.beacon_temp);
            tv_beacon_lux = itemView.findViewById(R.id.beacon_lux);
            tv_beacon_rssi = itemView.findViewById(R.id.beacon_rssi);
            rtv_beacon_lastseen = itemView.findViewById(R.id.beacon_lastseen);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

}
