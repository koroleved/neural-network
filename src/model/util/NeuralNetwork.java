package model.util;

//import com.google.gson.GsonBuilder;
import model.math.Matrix;
import model.math.Vec;
import model.optimizer.GradientDescent;
import model.optimizer.Optimizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork implements Serializable {

    private final CostFunction costFunction;
    private final int networkInputSize;
    private final double l2;
    private final Optimizer optimizer;

    private List<Layer> layers = new ArrayList<>();

    public NeuralNetwork() {
        this(new NeuralNetwork.Builder(100));
    }

    /**
     * Создаем нейронную сеть с конфигурацией заданной при помощи паттерна
     * строителем
     *
     * @param nb The config for the neural network
     */
    private NeuralNetwork(Builder nb) {
        costFunction = nb.costFunction;
        networkInputSize = nb.networkInputSize;
        optimizer = nb.optimizer;
        l2 = nb.l2;

        // Adding inputLayer
        Layer inputLayer = new Layer(networkInputSize, Activation.Identity);
        layers.add(inputLayer);

        Layer precedingLayer = inputLayer;

        for (int i = 0; i < nb.layers.size(); i++) {
            Layer layer = nb.layers.get(i);
            Matrix w = new Matrix(precedingLayer.size(), layer.size());
            nb.initializer.initWeights(w, i);
            layer.setWeights(w);    // Each layer contains the weights between preceding layer and itself
            layer.setOptimizer(optimizer.copy());
            layer.setL2(l2);
            layer.setPrecedingLayer(precedingLayer);
            layers.add(layer);
            precedingLayer = layer;
        }
    }

    /**
     * Вычисляет входной вектор, возвращая выходной сигнал сети
     */
    public Result evaluate(Vec input) {
        return evaluate(input, null);
    }

    /**
     * добавляет в результат возвращения стоймость
     */
    public Result evaluate(Vec input, Vec expected) {
        Vec signal = input;
        for (Layer layer : layers) {
            signal = layer.evaluate(signal);
        }

        if (expected != null) {
            learnFrom(expected);
            double cost = costFunction.getTotal(expected, signal);
            return new Result(signal, cost);
        }

        return new Result(signal);
    }

    /**
     * обучение без обновления весов (обратное распространение)
     */
    private void learnFrom(Vec expected) {
        Layer layer = getLastLayer();
        //ошибка
        Vec dCdO = costFunction.getDerivative(expected, layer.getOut());

        // обратное направление
        do {
            Vec dCdI = layer.getActivation().dCdI(layer.getOut(), dCdO);
            Matrix dCdW = dCdI.outerProduct(layer.getPrecedingLayer().getOut());

            // Сохраняем веса и смещения
            layer.addDeltaWeightsAndBiases(dCdW, dCdI);

            // prepare error propagation and store for next iteration
            dCdO = layer.getWeights().multiply(dCdI);

            layer = layer.getPrecedingLayer();
        } while (layer.hasPrecedingLayer());     // Stop when we are at input layer
    }

    /**
     * обновляем веса и смещения
     */
    public synchronized void updateFromLearning() {
        for (Layer l : layers) {
            if (l.hasPrecedingLayer()) // Skip input layer
            {
                l.updateWeightsAndBias();
            }
        }

    }

    // --------------------------------------------------------------------
    public List<Layer> getLayers() {
        return layers;
    }

    public String toJson(boolean pretty) {
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        if (pretty) gsonBuilder.setPrettyPrinting();
//        return gsonBuilder.create().toJson(new NetworkState(this));
        return null;
    }

    private Layer getLastLayer() {
        return layers.get(layers.size() - 1);
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append ("\n---------- NetWork Parameters -----------------")
                .append ("\nl2: ").append (l2)
                .append ("\nnetworkInputSize: ").append (networkInputSize)
                .append ("\ncostFunction: ").append (costFunction)
                .append ("\noptimaizer: ").append (optimizer)
                .append ("\n----- Layers --------");
        for (Layer tmp : layers) {
            sb.append ("\n").append (tmp);
        }
        return sb.toString();
    }
    // --------------------------------------------------------------------
    /**
     * Simple builder for a NeuralNetwork
     */
    public static class Builder {

        private List<Layer> layers = new ArrayList<>();
        private int networkInputSize;

        // defaults:
        private Initializer initializer = new Initializer.Random(-0.5, 0.5);
        private CostFunction costFunction = new CostFunction.Quadratic();
        private Optimizer optimizer = new GradientDescent(0.005);
        private double l2 = 0;

        public Builder(int networkInputSize) {
            this.networkInputSize = networkInputSize;
        }

        /**
         * Create a builder from an existing neural network, hence making it
         * possible to do a copy of the entire state and modify as needed.
         */
        public Builder(NeuralNetwork other) {
            networkInputSize = other.networkInputSize;
            costFunction = other.costFunction;
            optimizer = other.optimizer;
            l2 = other.l2;

            List<Layer> otherLayers = other.getLayers();
            for (int i = 1; i < otherLayers.size(); i++) {
                Layer otherLayer = otherLayers.get(i);
                layers.add(
                        new Layer(
                                otherLayer.size(),
                                otherLayer.getActivation(),
                                otherLayer.getBias()
                        )
                );
            }

            initializer = (weights, layer) -> {
                Layer otherLayer = otherLayers.get(layer + 1);
                Matrix otherLayerWeights = otherLayer.getWeights();
                weights.fillFrom(otherLayerWeights);
            };
        }

        public Builder initWeights(Initializer initializer) {
            this.initializer = initializer;
            return this;
        }

        public Builder setCostFunction(CostFunction costFunction) {
            this.costFunction = costFunction;
            return this;
        }

        public Builder setOptimizer(Optimizer optimizer) {
            this.optimizer = optimizer;
            return this;
        }

        public Builder l2(double l2) {
            this.l2 = l2;
            return this;
        }

        public Builder addLayer(Layer layer) {
            layers.add(layer);
            return this;
        }

        public NeuralNetwork create() {
            return new NeuralNetwork(this);
        }

    }

    // -----------------------------
    public static class NetworkState {

        String costFunction;
        Layer.LayerState[] layers;

        public NetworkState(NeuralNetwork network) {
            costFunction = network.costFunction.getName();

            layers = new Layer.LayerState[network.layers.size()];
            for (int l = 0; l < network.layers.size(); l++) {
                layers[l] = network.layers.get(l).getState();
            }
        }

        public Layer.LayerState[] getLayers() {
            return layers;
        }
    }

    public static void main(String[] args) {
        //NeuralNetwork.toJson(true);
    }
}
