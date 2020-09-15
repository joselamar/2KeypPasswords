package pt.ubi.di.pdm.joselamarao.a2keyppasswords;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class adapter extends RecyclerView.Adapter<adapter.adapterviewholder> {
    private ArrayList<Item> mItemList;
    private onItemClickListener mListener;

    public interface onItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        mListener=listener;
    }

    public static class adapterviewholder extends RecyclerView.ViewHolder{
        public TextView mTextView1;
        public TextView mTextView2;

        public adapterviewholder(@NonNull View itemView, final onItemClickListener listener) {
            super(itemView);
            mTextView1=itemView.findViewById(R.id.textViewHead);
            mTextView2=itemView.findViewById(R.id.textViewDesc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!=null){
                        int position=getAdapterPosition();
                        if(position!= RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public adapter(ArrayList<Item> list){
    mItemList=list;
    }

    @NonNull
    @Override
    public adapterviewholder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.line,parent,false);
        adapterviewholder avh=new adapterviewholder(v, mListener);
        return avh;
    }

    @Override
    public void onBindViewHolder(@NonNull adapterviewholder adapterviewholder, int i) {
        Item currentItem= mItemList.get(i);
        adapterviewholder.mTextView1.setText(currentItem.getText1());
        adapterviewholder.mTextView2.setText(currentItem.getText2());
    }
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void searchedlist(ArrayList<Item> lista){
        mItemList = lista;
        notifyDataSetChanged();

    }

}
