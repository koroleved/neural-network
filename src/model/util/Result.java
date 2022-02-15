package model.util;

import model.math.Vec;

public class Result {
    private final Vec output;
    private final Double cost;

    public Result(Vec output) {
        this.output = output;
        cost = null;
    }

    public Result(Vec output, double cost) {
        this.output = output;
        this.cost = cost;
    }

    public Vec getOutput() {
        return output;
    }

    public Double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return "Result{" + "output=" + output +
                ", cost=" + cost +
                '}';
    }
}
