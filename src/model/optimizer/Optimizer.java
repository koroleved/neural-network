package model.optimizer;

import java.io.Serializable;
import model.math.Matrix;
import model.math.Vec;

public interface Optimizer extends Serializable {

    void updateWeights(Matrix weights, Matrix dCdW);

    Vec updateBias(Vec bias, Vec dCdB);

    Optimizer copy();
}
