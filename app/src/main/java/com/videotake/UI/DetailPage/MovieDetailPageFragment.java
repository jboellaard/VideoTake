package com.videotake.UI.DetailPage;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.videotake.Domain.Movie;
import com.videotake.Domain.MovieList;
import com.videotake.Domain.Review;
import com.videotake.Logic.User.EmptyResult;
import com.videotake.Logic.User.LoggedInUserViewModel;
import com.videotake.Logic.User.LoginViewModel;
import com.videotake.Logic.User.UserViewModel;
import com.videotake.R;
import com.videotake.UI.Adapters.MovieListOverviewInPopupAdapter;
import com.videotake.databinding.FragmentDetailPageBinding;

import java.util.List;
import java.util.Objects;

public class MovieDetailPageFragment extends Fragment {
    private final String TAG_NAME = MovieDetailPageFragment.class.getSimpleName();
    private FragmentDetailPageBinding binding;
    private MovieDetailsViewModel movieDetailsViewModel;
    private LoginViewModel loginViewModel;
    private LoggedInUserViewModel loggedInUserViewModel;
    private UserViewModel userViewModel;
    private Movie movie;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDetailPageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        int moviePosition = MovieDetailPageFragmentArgs.fromBundle(getArguments()).getMovieId();

        //resources for strings
        Resources res = getResources();

        //getting the xml elements
        ImageView image = binding.movieImage;
        TextView title = binding.movieTitle;
        TextView description = binding.movieDescription;
        TextView date = binding.movieReleasedate;
        TextView rating = binding.movieRatingint;
        TextView genre = binding.movieGenre;
        ImageView adult = binding.movieAdult;
        Button addToListButton = binding.addmovietolistbutton;
        Button rateButton = binding.ratemoviebutton;

        movieDetailsViewModel = new ViewModelProvider(this).get(MovieDetailsViewModel.class);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        loggedInUserViewModel = new ViewModelProvider(this).get(LoggedInUserViewModel.class);

        List<Movie> movies = movieDetailsViewModel.getCurrentList();
        if (movies!=null) {
            movie = movies.get(moviePosition);

            //getting video link and reviews of this movie
            movieDetailsViewModel.getVideoLinkAndReviews(movie);
            movieDetailsViewModel.getVideoLinkAndReviewsResult().observe(
                    getViewLifecycleOwner(), (Observer<EmptyResult>) result -> {
                if (result == null) {
                    return;
                }
//                loadingProgressBar.setVisibility(View.GONE);
                if (result.getError() == null) {
                    String videoPath = movie.getVideoPath();
                    List<Review> reviews = movie.getReviews();
                    //code to show video
                } else {
                    Log.d(TAG_NAME, "An error occurred when trying to load the reviews and trailer");
                }
            });

            //assigning values to xml attributes
            title.setText(movie.getMovieName());
            description.setText(movie.getMovieDescription());
            rating.setText(String.valueOf(movie.getRating()));
            date.setText(movie.getReleaseDate());
            Picasso.with(inflater.getContext()).load("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath()).into(image);

            if (loginViewModel.getLoggedInUser()!=null){
                addToListButton.setVisibility(View.VISIBLE);
                addToListButton.setOnClickListener(view -> {
                    LayoutInflater popUpInflater = (LayoutInflater) inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView = popUpInflater.inflate(R.layout.popup_window_add_movie_to_list, null);
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
                    popupWindow.setElevation(20);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                    MovieListOverviewInPopupAdapter mAdapter = new MovieListOverviewInPopupAdapter(popupView.getContext(),
                            movie.getMovieID(),loggedInUserViewModel, ViewTreeLifecycleOwner.get(view));
                    RecyclerView mRecyclerView = popupView.findViewById(R.id.recyclerview_list);
                    mRecyclerView.setAdapter(mAdapter);
                    loggedInUserViewModel.lists();
                    loggedInUserViewModel.getListsResult().observe(
                            Objects.requireNonNull(ViewTreeLifecycleOwner.get(view)), result -> {
                        if (result == null) { return; }
                        if (result.getError() == null) {
                            List<MovieList> allLists = loggedInUserViewModel.getUserLists();
                            mAdapter.setData(allLists);
                        } else {
                            Log.d(TAG_NAME, "An error occurred when trying to load trending movies");
                        }
                    });
                    loggedInUserViewModel.getAddMovieToListResult().observe(
                            Objects.requireNonNull(ViewTreeLifecycleOwner.get(view)), result -> {
                        if (result == null) { return; }
                        if (result.getError() == null) {
                            Toast.makeText(inflater.getContext(), "Successfully added movie to list!", Toast.LENGTH_LONG).show();
                            popupWindow.dismiss();
                        } else {
                            Toast.makeText(inflater.getContext(), "Could not add movie to list", Toast.LENGTH_LONG).show();
                            Log.d(TAG_NAME, "An error occurred when trying add the movie to the list");
                        }
                        loggedInUserViewModel.resetAddMovieToListResult();
                    });
                    final Button cancelButton = popupView.findViewById(R.id.cancel_button);
                    cancelButton.setOnClickListener(v -> popupWindow.dismiss());
                });
            }
            rateButton.setOnClickListener(view -> {
                LayoutInflater popUpInflater = (LayoutInflater) inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = popUpInflater.inflate(R.layout.popup_window_rate, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
                popupWindow.setElevation(20);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                final RatingBar ratingValue = popupView.findViewById(R.id.rating_field);
                final Button addRatingButton = popupView.findViewById(R.id.rate_button);
                addRatingButton.setOnClickListener(v -> {
                    userViewModel.postRating(movie.getMovieID(),ratingValue.getRating());
                    userViewModel.getPostRatingResult().observe(getViewLifecycleOwner(), result -> {
                        if (result == null) { return; }
                        if (result.getError() == null) {
                            popupWindow.dismiss();
                            Toast.makeText(getLayoutInflater().getContext(), "Rating added!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG_NAME, "An error occurred trying to rate this movie");
                        }
                    });
                });
            });
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
