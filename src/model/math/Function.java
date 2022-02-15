package model.math;

import java.io.Serializable;

@FunctionalInterface
public interface Function extends Serializable {

    double apply(double value);
}
