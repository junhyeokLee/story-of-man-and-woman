package com.dev_sheep.story_of_man_and_woman.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dev_sheep.story_of_man_and_woman.R;
import com.dev_sheep.story_of_man_and_woman.data.database.entity.ItemImage;
import com.dev_sheep.story_of_man_and_woman.view.Assymetric.AGVRecyclerViewAdapter;
import com.dev_sheep.story_of_man_and_woman.view.Assymetric.AsymmetricItem;

import java.util.List;


class ChildAdapter extends AGVRecyclerViewAdapter<FeedImageHolder> {
    private final List<ItemImage> items;
    private int mDisplay = 0;
    private int mTotal = 0;

    public ChildAdapter(List<ItemImage> items, int mDisplay, int mTotal) {
        this.items = items;
        this.mDisplay = mDisplay;
        this.mTotal = mTotal;

    }




    @Override public FeedImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("RecyclerViewActivity", "onCreateView");

        return new FeedImageHolder(parent, viewType,items);
    }

    @Override public void onBindViewHolder(FeedImageHolder holder, int position) {
        Log.d("RecyclerViewActivity", "onBindView position=" + position);
        holder.bind(items,position,mDisplay,mTotal);

    }

    @Override public int getItemCount() {
        return items.size();
    }

    @Override public AsymmetricItem getItem(int position) {
        return (AsymmetricItem) items.get(position);
    }

    @Override public int getItemViewType(int position) {
        return position % 2 == 0 ? 1 : 0;
    }


}


class FeedImageHolder extends RecyclerView.ViewHolder {
    private final ImageView mImageView;
    private final TextView textView;

    public FeedImageHolder(ViewGroup parent, int viewType, List<ItemImage> items) {
        super(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_item, parent, false));

        mImageView = (ImageView) itemView.findViewById(R.id.mImageView);
        textView = (TextView) itemView.findViewById(R.id.tvCount);



    }


    public void bind(List<ItemImage> item, int position, int mDisplay, int mTotal) {
//        ImageLoader.getInstance().displayImage(String.valueOf(item.get(position).getImagePath()), mImageView);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(16));

        Glide.with(itemView.getContext())
                .load(item.get(position).getImagePath())
                .apply(requestOptions)
                .placeholder(android.R.color.transparent)
                .into(mImageView);

        textView.setText("+"+(mTotal-mDisplay));
        if(mTotal > mDisplay)
        {
            if(position  == mDisplay-1) {
                textView.setVisibility(View.VISIBLE);
                mImageView.setAlpha(72);
            }
            else {
                textView.setVisibility(View.INVISIBLE);
                mImageView.setAlpha(255);
            }
        }
        else
        {
            mImageView.setAlpha(255);
            textView.setVisibility(View.INVISIBLE);
        }

        // textView.setText(String.valueOf(item.getPosition()));
    }
}