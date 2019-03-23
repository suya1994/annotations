package com.example.annotation.food;

import com.example.annotations.Factory;

@Factory(id = "noodles", type = ChineseFood.class)
public class Noodles implements ChineseFood {
    @Override
    public float getPrice() {
        return 10.5f;
    }
}
