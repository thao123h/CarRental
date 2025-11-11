package com.example.carrental.modals.item;

import com.bumptech.glide.load.ImageHeaderParser;
import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.modals.enums.Category;
import com.example.carrental.modals.enums.ImageType;

import java.time.LocalDateTime;
import java.util.List;

public class ItemImageDTO {
    private Long id;
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public ItemImageDTO() {
    }

    public ItemImageDTO(Long id, String imageUrl, ImageType imageType) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    private ImageType imageType;
}
