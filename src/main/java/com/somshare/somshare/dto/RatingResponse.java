package com.somshare.somshare.dto;

public record RatingResponse(
    long likeCount,
    long dislikeCount,
    String userRating  // "like", "dislike", or null
) {}
