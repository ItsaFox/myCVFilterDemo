package d.blueshoestring.mycvdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecycleViewAdapterEntry extends RecyclerView.Adapter<RecycleViewAdapterEntry.MyViewHolder> {
    private ArrayList<CVEntry> listOfObjects;
    private EntryListener mEntryListener;
    private String version;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView EntryText, EntryText2;
        EntryListener onEntryListener;

        public MyViewHolder(@NonNull View itemView, EntryListener onEntryListener) {
            super(itemView);
            EntryText = itemView.findViewById(R.id.ListTextField);
            EntryText2 = itemView.findViewById(R.id.ListTextField2);
            this.onEntryListener = onEntryListener;
            itemView.setOnClickListener(this);
        }

        public TextView getTextView(){
            return EntryText;
        }

        public TextView getTextView2(){
            return EntryText2;
        }

        @Override
        public void onClick(View v) {
            onEntryListener.onEntryClick(getAdapterPosition());
        }
    }

    public RecycleViewAdapterEntry(ArrayList<CVEntry> listOfObjects, EntryListener entryListener, String version) {
        this.listOfObjects = listOfObjects;
        this.mEntryListener = entryListener;
        this.version = version;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycleview, parent,false);
        if(version.equals("Happy")){                //Changes the color of the cards if the version is happy.
            CardView cardView = view.findViewById(R.id.card_view);
            cardView.setCardBackgroundColor(0xFFB8FF78);
        }
        return new MyViewHolder(view,mEntryListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.getTextView().setText(listOfObjects.get(position).getTitle());                                               //Title on the card.
        holder.getTextView2().setText(listOfObjects.get(position).getStart() + " " + listOfObjects.get(position).getEnd()); //Timeframe on the card.
        if(listOfObjects.get(position).getStart() != null){                                                                 //If there is no timeframe then none should be shown.
            holder.getTextView2().setVisibility(View.VISIBLE);
        }else{
            holder.getTextView2().setVisibility(View.GONE);
        }
        if(listOfObjects.get(position).isHighlighted()){                //Sets the highlight if the entry is highlighted.
            holder.getTextView().setBackgroundColor(0xFFFFFF00);
        }else{
            if(version.equals("Happy")){                                //The colors are changed if the happy version has been selected.
                holder.getTextView().setBackgroundColor(0xFFB8FF78);
            }else{
                holder.getTextView().setBackgroundColor(0xFFFAFAFA);
            }
        }
    }

    @Override
    public int getItemCount() {
        return listOfObjects.size();
    }

    public interface EntryListener{
        void onEntryClick(int position);

    }
}