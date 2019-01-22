package work.demo.com.cameracropselfstudy.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.LinkedList;

import work.demo.com.cameracropselfstudy.Activity.VideoListActivity;
import work.demo.com.cameracropselfstudy.Interface.RecyclerViewItemOnClickListener;
import work.demo.com.cameracropselfstudy.ModelPackage.Model_Video;
import work.demo.com.cameracropselfstudy.R;

/**
 * Created by su on 3/15/18.
 */

public class Adapter_VideoFolder extends RecyclerView.Adapter<Adapter_VideoFolder.ViewHolder> {

    private Activity context;
    private LinkedList<Model_Video> linkedList = new LinkedList();
    private RecyclerViewItemOnClickListener recyclerViewItemOnClickListener = null;


    public Adapter_VideoFolder(LinkedList<Model_Video> al_video, Activity activity) {
        this.context = activity;
        this.linkedList = al_video;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.model_view_of_video_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewItemOnClickListener.onItemClick(position);
            }
        });
        holder.tv_1.setText(SplitStringToArray(linkedList.get(position).getStr_path()).
                get(SplitStringToArray(linkedList.get(position).getStr_path()).size() - 1));

        Glide.with(context).load(linkedList.get(position).getStr_path())
                .skipMemoryCache(false)
                .into(holder.imageView);


    }

    @Override
    public int getItemCount() {
        return linkedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView tv_1;
        ConstraintLayout parent_layout;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            tv_1 = itemView.findViewById(R.id.tv_1);
            parent_layout = itemView.findViewById(R.id.parent_layout);
        }
    }

    public void SetItemClickListener(RecyclerViewItemOnClickListener recyclerViewItemOnClickListener){
        this.recyclerViewItemOnClickListener = recyclerViewItemOnClickListener;
    }

    public void ClearData(){
        linkedList.clear();
        notifyDataSetChanged();
    }

    public LinkedList<Model_Video> getData(){
        return linkedList;
    }


    private ArrayList<String> SplitStringToArray(String s) {
        String[] strings = s.split("/");
        ArrayList<String> splitted_strings = new ArrayList<>();

        for (String s1 : strings) {
            splitted_strings.add(s1);
        }

        return splitted_strings;
    }
}
