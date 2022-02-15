package train;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.math.Vec;
import model.optimizer.GradientDescent;
import model.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.shuffle;
import static java.util.Collections.unmodifiableList;
import static model.math.SharedRnd.getRnd;
import static model.math.SharedRnd.setRnd;
import static model.util.Activation.*;

public class TrainNetworkMnist {
    private static Logger log = LoggerFactory.getLogger(TrainNetworkMnist.class);

    private static int BATCH_SIZE = 32;


    public static void main(String[] args) throws IOException {
        int seed = 942457;
        setRnd(new Random(seed));

        List<DigitData> trainData = FileUtil.loadImageData("train");
        System.out.println("Обучающий набор загружен");
        List<DigitData> testData = FileUtil.loadImageData("t10k");
        System.out.println("Набор для тестирования загружен");

        for (DigitData d : trainData) {
            d.setRandom(new Random(seed++));
        }
//Leaky_ReLU были все
        model.util.NeuralNetwork network =
                new NeuralNetwork.Builder(28 * 28)
                        .addLayer(new model.util.Layer(64, Softplus))
                      //  .addLayer(new model.util.Layer(64, Sigmoid))
                        .addLayer(new Layer(10, Softmax))
                        .initWeights(new Initializer.XavierNormal())
                        .setCostFunction(new CostFunction.Quadratic())
                        // .setOptimizer(new Nesterov(0.01683893848216524, 0.89339285078484840))
                        .setOptimizer(new GradientDescent(0.05))

                        //.l2(0.00011126201713636 / 256)
                        .create();

        int epoch = 0;
        double errorRateOnTrainDS;
        double errorRateOnTestDS;
        StopEvaluator evaluator = new StopEvaluator(network, 40, null);
        boolean shouldStop = false;
        long t0 = currentTimeMillis();
        do {
            epoch++;
            shuffle(trainData, getRnd());//перемешиваем

            int correctTrainDS = applyDataToNet(trainData, network, true);
            errorRateOnTrainDS = 100 - (100.0 * correctTrainDS / trainData.size());

            if (epoch % 5 == 0) {
                int correctOnTestDS = applyDataToNet(testData, network, false);
                errorRateOnTestDS = 100 - (100.0 * correctOnTestDS / testData.size());
                shouldStop = evaluator.stop(errorRateOnTestDS);
                double epocsPerMinute = epoch * 60000.0 / (currentTimeMillis() - t0);
                log.info(format("Epoch: %3d    |   Train error rate: %6.3f %%    |   Test error rate: %5.2f %%   |   Epocs/min: %5.2f", epoch, errorRateOnTrainDS, errorRateOnTestDS, epocsPerMinute));
            } else {
                log.info(format("Epoch: %3d    |   Train error rate: %6.3f %%    |", epoch, errorRateOnTrainDS));
            }

            trainData.parallelStream().forEach(DigitData::transformDigit);
        } while (!shouldStop);
        double lowestErrorRate = evaluator.getLowestErrorRate();
        log.info(format("No improvement, aborting. Reached a lowest error rate of %7.4f %%", lowestErrorRate));
        //  writeFile(evaluator, lowestErrorRate);
        //  LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        //  String output = today.toString();
        //   String path = (evaluator.getLowestErrorRate() + "_" + output + " "+evaluator.getLowestErrorRate()+".bin");
        //    String path1 = ("Vec");
        //      SerializationUtils.serialize(network, path);
    }

    /**
     * Run the entire dataset <code>data</code> through the network.
     * If <code>learn</code> is true the network will learn from the data.
     */
    private static int applyDataToNet(List<DigitData> data, NeuralNetwork network, boolean learn) {
        final AtomicInteger correct = new AtomicInteger();//для подсчета в паралели.

        for (int i = 0; i <= data.size() / BATCH_SIZE; i++) {  //распараллеливания мини-пакетного выполнения получаем по 32 изображения

            getBatch(i, data).parallelStream().forEach(img -> {
                Vec input = new Vec(img.getData());
                Result result = learn ?
                        network.evaluate(input, new Vec(img.getLabelAsArray())) :  //getLabelAsArray() номер цифры в выходном массиве проверка при обучении
                        network.evaluate(input);                                    //проверка при тестировании

                if (result.getOutput().indexOfLargestElement() == img.getLabel())       //результат верно или нет
                    correct.incrementAndGet();
            });

            if (learn)//обновляем веса только на тренировачных данных
                network.updateFromLearning();
        }
        return correct.get();
    }

    /**
     * Cuts out batch i from dataset data.
     */
    //берем пакет для распаралеливания
    private static List<DigitData> getBatch(int i, List<DigitData> data) {
        int fromIx = i * BATCH_SIZE;
        int toIx = Math.min(data.size(), (i + 1) * BATCH_SIZE);
        return unmodifiableList(data.subList(fromIx, toIx));
    }

    /**
     * Saves the weights and biases of the network in directory
     */
    private static void writeFile(StopEvaluator evaluator, double lowestErrorRate) throws IOException {
        File outDir = new File("F:\\Диплом\\MyDiplom\\src");
        if (!outDir.exists())
            if (!outDir.mkdirs())
                throw new IOException("Could not create directory " + outDir.getAbsolutePath());

        File outFile = new File(outDir, format("%4.2f %tF .json", lowestErrorRate, new Date()));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))) {
            bw.write(evaluator.getBestNetSoFar());
        }
    }

    private static void writeJson(String path, NeuralNetwork network) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(network, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
