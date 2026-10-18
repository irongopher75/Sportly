package com.turfmatch.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.turfmatch.app.R;
import com.turfmatch.app.model.GameModel;

import java.util.List;

// CO3 - Custom Adapter for Game Listings ListView with Sport Icons
public class GameAdapter extends BaseAdapter {
    private final Context context;
    private final List<GameModel> gameList;
    private final LayoutInflater inflater;

    public GameAdapter(Context context, List<GameModel> gameList) {
        this.context = context;
        this.gameList = gameList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return gameList.size();
    }

    @Override
    public Object getItem(int position) {
        return gameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_game, parent, false);
        }

        GameModel game = gameList.get(position);

        ImageView ivSportIcon = convertView.findViewById(R.id.ivSportIcon);
        TextView tvGameTitle = convertView.findViewById(R.id.tvGameTitle);
        TextView tvTurfAndSport = convertView.findViewById(R.id.tvTurfAndSport);
        TextView tvDateTime = convertView.findViewById(R.id.tvDateTime);

        tvGameTitle.setText(game.getTitle());
        tvTurfAndSport.setText(game.getTurfName() + " • " + game.getSport() + " (" + game.getCompetitiveLevel() + ")");
        tvDateTime.setText(game.getDate() + " at " + game.getTime());

        // Set sport icon based on sport type
        String sport = game.getSport();
        if (sport != null) {
            switch (sport.toLowerCase()) {
                case "cricket":
                    ivSportIcon.setImageResource(R.drawable.ic_cricket);
                    break;
                case "badminton":
                    ivSportIcon.setImageResource(R.drawable.ic_badminton);
                    break;
                case "tennis":
                    ivSportIcon.setImageResource(R.drawable.ic_tennis);
                    break;
                case "basketball":
                    ivSportIcon.setImageResource(R.drawable.ic_basketball);
                    break;
                case "football":
                default:
                    ivSportIcon.setImageResource(R.drawable.ic_football);
                    break;
            }
        } else {
            ivSportIcon.setImageResource(R.drawable.ic_football);
        }

        return convertView;
    }
}
