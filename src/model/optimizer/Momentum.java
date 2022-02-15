package model.optimizer;

import model.math.Matrix;
import model.math.Vec;

public class Momentum implements Optimizer{
    private double learningRate;
    private double momentum;
    private Matrix lastDW;
    private Vec lastDBias;

    public Momentum(double learningRate, double momentum) {
        this.learningRate = learningRate;
        this.momentum = momentum;
    }

    public Momentum(double learningRate) {
        this(learningRate, 0.9);
    }

    @Override
    public void updateWeights(Matrix weights, Matrix dCdW) {
        if (lastDW == null) {
            lastDW = dCdW.copy().mul(learningRate);
        } else {
            lastDW.mul(momentum).add(dCdW.copy().mul(learningRate));
        }
        weights.sub(lastDW);
    }

    @Override
    public Vec updateBias(Vec bias, Vec dCdB) {
        if (lastDBias == null) {
            lastDBias = dCdB.mul(learningRate);
        } else {
            lastDBias = lastDBias.mul(momentum).add(dCdB.mul(learningRate));
        }
        return bias.sub(lastDBias);
    }

    @Override
    public Optimizer copy() {
        return new Momentum(learningRate, momentum);
    }
}
