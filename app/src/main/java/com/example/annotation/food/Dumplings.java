package com.example.annotation.food;

import com.example.annotations.Factory;

@Factory(id = "dumplings", type = ChineseFood.class)
public class Dumplings implements ChineseFood {
    @Override
    public float getPrice() {
        return 16f;
    }
}
