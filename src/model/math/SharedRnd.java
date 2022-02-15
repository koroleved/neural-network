package model.math;

import java.util.Random;

public class SharedRnd {
    private static Random rnd = new Random();

    public static Random getRnd() {
        return rnd;
    }

    public static void setRnd(Random rnd) {
        SharedRnd.rnd = rnd;
    }
}
