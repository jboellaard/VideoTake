package com.videotake.Domain;

public class Review {
    private String reviewID;
    private String userName;
    private String content;
    private double rating;

    public Review(String reviewID, String userName, String content, double rating) {
        this.reviewID = reviewID;
        this.userName = userName;
        this.content = content;
        this.rating = rating;
    }


}
