package com.moutamid.torahsharee.model;

import java.util.List;

public class PostModel {
    public String name, date, caption,
            video_link, profile_link, my_uid, push_key;
    public int share_count, comment_count;
    public List<String> tagsList;
    public String thumbnailUrl;

    public PostModel() {
    }
}