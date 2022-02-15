package train;

import data.SerializationUtils;
import model.math.Vec;
import model.optimizer.GradientDescent;
import model.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
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

public class TrainMyDataSet {
    private static Logger log = LoggerFactory.getLogger(TrainNetworkMnist.class);
    private static int BATCH_SIZE = 32;
    public static void main(String[] args) throws IOException {
        int seed = 942457;
        setRnd(new Random(seed));
        String path = ("C:\\Users\\dennn\\PycharmProjects\\untitled2\\characters");
        List<DigitData> trainData = FileUtil.loadImageDataNumbers(path);
        System.out.println("Обучающий набор загружен, его размер равен = "+trainData.size());
        NeuralNetwork network =
                new NeuralNetwork.Builder(28 * 28)
                        .addLayer(new model.util.Layer(64, ReLU))
                        .addLayer(new model.util.Layer(64, ReLU))
                        .addLayer(new Layer(10, Softmax))
                        .initWeights(new Initializer.XavierNormal())
                        .setCostFunction(new CostFunction.Quadratic())
                        // .setOptimizer(new Nesterov(0.01683893848216524, 0.89339285078484840))
                        .setOptimizer(new GradientDescent(0.001))
                        //.l2(0.00011126201713636 / 256)
                        .create();
        int epoch = 0;
        double errorRateOnTrainDS;
        StopEvaluator evaluator = new StopEvaluator(network, 40, null);
        boolean shouldStop = false;
        long t0 = currentTimeMillis();
        do {
            epoch++;
            shuffle(trainData, getRnd());//перемешиваем

            int correctTrainDS = applyDataToNet(trainData, network, true);
            errorRateOnTrainDS = 100 - (100.0 * correctTrainDS / trainData.size());

            if (epoch % 5 == 0) {
                shouldStop = evaluator.stop(errorRateOnTrainDS);
                double epocsPerMinute = epoch * 999.0 / (currentTimeMillis() - t0);
                log.info(format("Epoch: %3d    |   Train error rate: %6.30f %%    |     Epocs/min: %5.2f", epoch, errorRateOnTrainDS, epocsPerMinute));
            } else {
                log.info(format("Epoch: %3d    |   Train error rate: %6.3f %%    |", epoch, errorRateOnTrainDS));
            }
            if (epoch==600)
            {
                shouldStop=true;
            }
            trainData.parallelStream().forEach(DigitData::transformDigit);
        } while (!shouldStop);
        double lowestErrorRate = evaluator.getLowestErrorRate();
        log.info(format("No improvement, aborting. Reached a lowest error rate of %7.4f %%", lowestErrorRate));
         // writeFile(evaluator, lowestErrorRate);
          LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
          String output = today.toString();
           String path1 = (evaluator.getLowestErrorRate() + "_" + output + " "+evaluator.getLowestErrorRate()+".bin");
            // String path1 = ("Vec");
              SerializationUtils.serialize(network, path1);
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

}


