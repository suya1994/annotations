package com.example.annotation;

import com.example.annotation.food.ChineseFood;
import com.example.annotation.food.Noodles;
import com.example.annotation.food.Restaurant;

import java.io.IOException;
import java.util.ArrayList;

public class Test {
    private ArrayList<Noodles> mList = new ArrayList<>();
    private static class Neibu{

    }

    public static void main(String[] args) throws IOException {
        Test test = new Test();
        test.mList.add(new Noodles());
        System.out.println("Noodles Name: " + Noodles.class.getName());
        System.out.println("Noodles CanonicalName: " + Noodles.class.getCanonicalName());

        System.out.println("Noodles in list Name: " + test.mList.get(0).getClass().getName());
        System.out.println("Noodles in list CanonicalName: " + test.mList.get(0).getClass().getCanonicalName());

        System.out.println("Noodles Name: " + Neibu.class.getName());
        System.out.println("Noodles CanonicalName: " + Neibu.class.getCanonicalName());


        System.out.println("Noodles Name: " + new Neibu(){}.getClass().getName());
        System.out.println("Noodles CanonicalName: " + new Neibu(){}.getClass().getCanonicalName());
    }
}
