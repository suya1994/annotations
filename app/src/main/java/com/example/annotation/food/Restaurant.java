package com.example.annotation.food;

import java.io.IOException;

public class Restaurant {

    public static void main(String[] args) throws IOException {
        ChineseFoodFactory factory = new ChineseFoodFactory();
        ChineseFood food = factory.create("dumplings");
        System.out.println("dumplings's price is " + food.getPrice());

    }
}
