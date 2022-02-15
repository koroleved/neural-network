package model.optimizer;

import model.math.Matrix;
import model.math.Vec;

public class GradientDescent implements Optimizer{

    private double learningRate;

    public GradientDescent(double learningRate) {
        this.learningRate = learningRate;
    }

    @Override
    public void updateWeights(Matrix weights, Matrix dCdW) {
        weights.sub(dCdW.mul(learningRate));
    }

    @Override
    public Vec updateBias(Vec bias, Vec dCdB) {
        return bias.sub(dCdB.mul(learningRate));
    }

    @Override
    public Optimizer copy() {
        // no need to make copies since this optimizer has
        // no state. Same instance can serve all layers.
        return this;
    }
}
