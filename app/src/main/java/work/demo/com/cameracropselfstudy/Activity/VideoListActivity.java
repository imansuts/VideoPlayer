package work.demo.com.cameracropselfstudy.Activity;

import android.app.ActivityManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import work.demo.com.cameracropselfstudy.Adapter.Adapter_VideoFolder;
import work.demo.com.cameracropselfstudy.ConstantPkg.Constant;
import work.demo.com.cameracropselfstudy.Interface.RecyclerViewItemOnClickListener;
import work.demo.com.cameracropselfstudy.ModelPackage.Model_Video;
import work.demo.com.cameracropselfstudy.R;

public class VideoListActivity extends AppCompatActivity {

    Adapter_VideoFolder obj_adapter;
    LinkedList<Model_Video> al_video = new LinkedList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewLayoutManager;
    private static final int REQUEST_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
//        loadProcessInfo();
        init();
        fn_video();
    }

    private void loadProcessInfo() {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = manager
                .getRunningAppProcesses();

        if (runningProcesses != null && runningProcesses.size() > 0) {

            for (int i = 0; i<runningProcesses.size(); i++){
                Log.d("list_" +i+"_",  runningProcesses.get(i).processName);
            }

        } else {
            Toast.makeText(getApplicationContext(),
                    "No running process found.", Toast.LENGTH_LONG).show();
        }
    }

    private void init(){

        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

//        fn_checkpermission();

    }


    public void fn_video() {

        int int_position = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name,column_id,thum;

        String absolutePathOfImage = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media._ID,MediaStore.Video.Thumbnails.DATA};

        final String orderBy = MediaStore.Images.Media.DISPLAY_NAME;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " ASC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        column_id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        thum = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            Log.e("Column", absolutePathOfImage);
            Log.e("Folder", cursor.getString(column_index_folder_name));
            Log.e("column_id", cursor.getString(column_id));
            Log.e("thum", cursor.getString(thum));

            Model_Video obj_model = new Model_Video();
            obj_model.setBoolean_selected(false);
            obj_model.setStr_path(absolutePathOfImage);
            obj_model.setStr_thumb(cursor.getString(thum));

            al_video.add(obj_model);

        }


        /*Log.d("chck_lis: ", String.valueOf(al_video.size()));
        Log.d("chk_path: ", al_video.get(10).getStr_path());
        Log.d("chk_thumb: ", al_video.get(10).getStr_thumb());*/

        obj_adapter = new Adapter_VideoFolder(al_video,VideoListActivity.this);
        recyclerView.setAdapter(obj_adapter);
        obj_adapter.SetItemClickListener(new RecyclerViewItemOnClickListener() {
            @Override
            public void onItemClick(int position) {
               String s = obj_adapter.getData().get(position).getStr_path();
//                Toast.makeText(VideoListActivity.this, s, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VideoListActivity.this, MainActivity.class);
                intent.setAction(Constant.ACTION.INIT_ACTION);
                intent.putExtra("video_path", s);
                startActivity(intent);
            }
        });

    }
}
