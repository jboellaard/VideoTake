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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.videotake.Domain.Movie;
import com.videotake.Logic.User.EmptyResult;
import com.videotake.Logic.User.LoggedInUserViewModel;
import com.videotake.Logic.User.LoginViewModel;
import com.videotake.Logic.User.UserViewModel;
import com.videotake.R;
import com.videotake.UI.Adapters.ReviewListAdapter;
import com.videotake.VideoTake;
import com.videotake.databinding.FragmentDetailPageBinding;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailPageFragment extends Fragment {
    private final String TAG_NAME = MovieDetailPageFragment.class.getSimpleName();
    private FragmentDetailPageBinding binding;
    private MovieDetailsViewModel movieDetailsViewModel;
    private LoginViewModel loginViewModel;
    private LoggedInUserViewModel loggedInUserViewModel;
    private UserViewModel userViewModel;
    private Movie movie;
    private ReviewListAdapter reviewListAdapter;

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
        Button addReview = binding.addReviewButton;

        reviewListAdapter = new ReviewListAdapter(inflater.getContext());
        RecyclerView reviewRecyclerView = binding.recyclerviewReviews;
        reviewRecyclerView.setAdapter(reviewListAdapter);

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
                if (result == null) { return; }
                if (result.getError() == null) {
                    String videoPath = movie.getVideoPath();
                    Log.d(TAG_NAME, "Amount of reviews: " + movie.getReviews().size());
                    TextView noReview = binding.noReviewsFound;
                    if (movie.getReviews().size()==0) {
                        addReview.setVisibility(View.VISIBLE);
                        noReview.setVisibility(View.VISIBLE);
                    }
                    ConstraintLayout expandReviews = binding.expandReviewsField;
                    reviewListAdapter.setData(movie.getReviews());
                    expandReviews.setOnClickListener(view -> {
                        addReview.setVisibility(View.VISIBLE);
                        reviewRecyclerView.setVisibility(View.VISIBLE);
                    });

                    //code to show video
                } else {
                    Log.d(TAG_NAME, "An error occurred when trying to load the reviews and trailer");
                }
            });


            //assigning values to xml attributes
            title.setText(movie.getMovieName());
            description.setText(movie.getMovieDescription());
            rating.setText(String.valueOf(movie.getVoteAverage()));
            date.setText(movie.getReleaseDate());
            Picasso.with(inflater.getContext()).load("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath()).into(image);

            if (loginViewModel.getLoggedInUser()!=null){
                addToListButton.setVisibility(View.VISIBLE);
                addToListButton.setOnClickListener(view -> {
                    LayoutInflater popUpInflater = (LayoutInflater) inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    VideoTake.showAddToListPopup(popUpInflater,view,movie.getMovieID(),loggedInUserViewModel,
                            TAG_NAME,inflater);
                });
            }
            rateButton.setOnClickListener(view -> {
                LayoutInflater popUpInflater = (LayoutInflater) inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = popUpInflater.inflate(R.layout.popup_window_rate, null);
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
                popupWindow.setElevation(20);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                final Button addRatingButton = popupView.findViewById(R.id.rate_button);
                addRatingButton.setOnClickListener(v -> {
                    RatingBar ratingValue = popupView.findViewById(R.id.rating_field);
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
