package com.videotake.DAL;

import com.videotake.Domain.GuestUser;
import com.videotake.Domain.LoggedInUser;
import com.videotake.Domain.MovieList;

import java.util.List;
import java.util.concurrent.Executor;

public class UserRepository {
    private final String TAG_NAME = UserRepository.class.getSimpleName();

    private static volatile UserRepository instance;

    private final Executor executor;
    private UserApiDAO userDAO;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;
    private GuestUser guestUser = null;

    private List<MovieList> userLists;

    // private constructor : singleton access
    private UserRepository(UserApiDAO userDAO, Executor executor) {
        this.executor = executor;
        this.userDAO = userDAO;
    }

    public static UserRepository getInstance(UserApiDAO userDAO, Executor executor) {
        if (instance == null) {
            instance = new UserRepository(userDAO,executor);
        }
        return instance;
    }

//    public boolean isLoggedIn() {
//        return user != null;
//    }

    public void logout() {
        user = null;
        userDAO.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    private void setGuestUser(GuestUser guestUser) {
        this.guestUser = guestUser;
    }

    public LoggedInUser getLoggedInUser(){
        return this.user;
    }

    public List<MovieList> getUserLists() { return this.userLists; }

    private void setUserLists(List<MovieList> userLists) { this.userLists = userLists; }

    public void login(String username, String password, final RepositoryCallback<LoggedInUser> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Result<LoggedInUser> result = userDAO.login(username, password);
                if (result instanceof Result.Success) {
                    setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
                }
                callback.onComplete(result);
            }
        });
    }

    public void useAsGuest(final RepositoryCallback<GuestUser> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Result<GuestUser> result = userDAO.createGuestSession();
                if (result instanceof Result.Success) {
                    setGuestUser(((Result.Success<GuestUser>) result).getData());
                }
                callback.onComplete(result);
            }
        });
    }

    public void lists(final RepositoryCallback<List<MovieList>> callback) {
        if (user!=null){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Result<List<MovieList>> result = userDAO.lists(user.getSession_Id());
                    if (result instanceof Result.Success) {
                        setUserLists(((Result.Success<List<MovieList>>) result).getData());
                    }
                    callback.onComplete(result);
                }
            });
        }
    }
}
