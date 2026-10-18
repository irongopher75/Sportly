package com.turfmatch.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.turfmatch.app.R;
import com.turfmatch.app.adapter.GameAdapter;
import com.turfmatch.app.model.GameModel;
import com.turfmatch.app.repository.FirebaseGameRepository;
import com.turfmatch.app.ui.detail.GameDetailActivity;

import java.util.ArrayList;
import java.util.List;

// CO3 - Home Fragment displaying Game Feed via custom ListView Adapter
public class HomeFragment extends Fragment {
    private ListView listViewGames;
    private GameAdapter adapter;
    private List<GameModel> gameList;
    private FirebaseGameRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listViewGames = view.findViewById(R.id.listViewGames);
        gameList = new ArrayList<>();
        adapter = new GameAdapter(getContext(), gameList);
        listViewGames.setAdapter(adapter);

        repository = new FirebaseGameRepository();

        repository.getGames(games -> {
            gameList.clear();
            gameList.addAll(games);
            adapter.notifyDataSetChanged();
        });

        listViewGames.setOnItemClickListener((parent, view1, position, id) -> {
            GameModel selectedGame = gameList.get(position);
            Intent intent = new Intent(getActivity(), GameDetailActivity.class);
            intent.putExtra("gameId", selectedGame.getId());
            intent.putExtra("title", selectedGame.getTitle());
            intent.putExtra("sport", selectedGame.getSport());
            intent.putExtra("turfName", selectedGame.getTurfName());
            intent.putExtra("date", selectedGame.getDate());
            intent.putExtra("time", selectedGame.getTime());
            intent.putExtra("level", selectedGame.getCompetitiveLevel());
            startActivity(intent);
        });

        return view;
    }
}
