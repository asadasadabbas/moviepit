package com.moviepit.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.moviepit.R;
import com.moviepit.model.CharacterModel;

import java.util.ArrayList;
public class CreditListAdapter  extends RecyclerView.Adapter<CreditListAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<CharacterModel> characterList;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView characterOriginalName, characterName;
        ImageView characterThumbnail;
        ProgressBar progressBarImage;

        MyViewHolder(View view) {
            super(view);
            characterOriginalName = (TextView) view.findViewById(R.id.original_name);
            characterName = (TextView) view.findViewById(R.id.character_name);
            characterThumbnail = (ImageView) view.findViewById(R.id.character_thumbnail);
            progressBarImage = (ProgressBar) view.findViewById(R.id.progress_bar_image);
        }

    }

    public CreditListAdapter(Context mContext, ArrayList<CharacterModel> characterList) {
        this.mContext = mContext;
        this.characterList = characterList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.credit_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final CharacterModel characterModel = characterList.get(position);

        holder.characterName.setText(characterModel.getCharacterName());
        holder.characterOriginalName.setText(characterModel.getOriginalName());

        Glide.with(mContext)
                .load(characterModel.getProfilePath())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBarImage.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBarImage.setVisibility(View.GONE);
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.characterThumbnail);
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }
}