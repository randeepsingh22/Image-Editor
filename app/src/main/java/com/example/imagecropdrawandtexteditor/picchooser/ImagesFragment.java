
package com.example.imagecropdrawandtexteditor.picchooser;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import com.example.imagecropdrawandtexteditor.R;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class ImagesFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater,
        final ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery, null);

        Cursor cur = getActivity().getContentResolver()
            .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN,MediaStore.Images.Media.SIZE },
                null,
                null,MediaStore.Images.Media.DATE_MODIFIED + " DESC");

        final List<GridItem> images = new ArrayList<GridItem>(cur.getCount());

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    if(cur.getString(1) != null && cur.getString(2) != null && cur.getString(3) != null){
                        images.add(new GridItem(cur.getString(1), cur.getString(0),cur.getString(2),cur.getLong(3)));
                    }

                    cur.moveToNext();
                }
            }
            cur.close();
        }

        GridView grid = (GridView) v.findViewById(R.id.grid);
        grid.setAdapter(new GalleryAdapter(getActivity(), images));
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
                ((SelectPictureActivity) getActivity()).imageSelected(images
                    .get(position).path,images
                    .get(position).imageTaken,images
                    .get(position).imageSize);
            }
        });
        return v;
    }

}
