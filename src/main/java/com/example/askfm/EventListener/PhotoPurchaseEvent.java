package com.example.askfm.EventListener;

import com.example.askfm.model.Photo;
import com.example.askfm.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PhotoPurchaseEvent {
    private final User Buyer;
    private final Photo photo;

}
