/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neural.network;

import data.Data;
import data.Filter;
import data.SerializationUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import model.math.Vec;
import model.util.NeuralNetwork;
import model.util.Result;

import javax.imageio.ImageIO;

/**
 *  Десериализация и проверка вывода результата
 */
public class NNMain {

    private static String Mnist = "C:\\Users\\dennn\\Desktop\\neural-network\\1.7976931348623157E308_2020-06-26.bin";
    private static String MyDataSet="0.0_2020-08-31 0.0.bin";
    public NNMain() throws IOException {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        /*NeuralNetwork nn = new NeuralNetwork();
        System.out.println("Original network");
        System.out.println(nn);
        try {
            SerializationUtils.serialize(nn, file);
        } catch (IOException ex) {
            System.out.println("Error #1: " + ex.getMessage());
        }*/
        NeuralNetwork MnistNN = new NeuralNetwork();
        NeuralNetwork MyDatasetNN= new NeuralNetwork();
        try {
            MnistNN = (NeuralNetwork) SerializationUtils.deserialize(Mnist);
            MyDatasetNN=(NeuralNetwork) SerializationUtils.deserialize(MyDataSet);
           if (MnistNN != null && MnistNN instanceof NeuralNetwork) {
                System.out.println("Restore network");
                System.out.println((NeuralNetwork) MnistNN);

            } else {
                System.out.println("Error #4: Invalid type");
            }
        } catch (IOException ex) {
            System.out.println("Error #2: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("Error #3: " + ex.getMessage());
        }

        File inputFile = new File("C:\\Users\\dennn\\Desktop\\for_test\\002_1.PNG2.png");//изображение для проверки
        BufferedImage inImage = ImageIO.read(inputFile);
        Filter filter = new Filter();
        double[] imgDouble = Data.createArrayfromImage(filter.resizeBufferedImage(inImage));//маштабируем и конвертируем в массив
        System.out.println(imgDouble.length);
        Vec img = new Vec(imgDouble);//слздаем вектор из массива
        Result result = MnistNN.evaluate(img);//получаем результат распознавания нейронной сети
        Result result1=MyDatasetNN.evaluate(img);
        System.out.println(result.toString());//выводим результат распознавания
        System.out.println(result1.toString());

    }
}
