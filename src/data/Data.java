package data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Data implements IData{


    @Override
    public File getFile(String pathFile) {
        return new File(pathFile);
    }

    @Override
    public List<File> getArrFile(String folder) {
        List<File> listFile=new ArrayList<>();
        File dir = new File(folder);
        if (dir.isDirectory()) {
            File[] arr = dir.listFiles();
            Collections.addAll(listFile,arr);
        }
        else
        {
            System.out.println("Вы выбрали не директорию");
        }
        return listFile;
    }

    @Override
    public BufferedImage getBufferedImage(File file) throws IOException {
        BufferedImage inImage = ImageIO.read(file);
        return inImage;
    }

    public static double[] createArrayfromImage(BufferedImage inImage) { //изображение представляем в виде набора 0 и 1  -  0 - белый цвет
        if (inImage.getHeight() * inImage.getWidth() > 115000) {
            return new double[1];
        } else {
            double[] arr = new double[inImage.getHeight() * inImage.getWidth()];
            int i = 0;
            for (int y = 0; y < inImage.getHeight(); y++) {
                for (int x = 0; x < inImage.getWidth(); x++) {
                    if (inImage.getRGB(x, y) == -1) {
                        arr[i] = 0;
                    } else {
                        arr[i] = 1;
                    }
                    i++;
                }
            }
            return arr;
        }
    }
    public static byte[] createByteArray(BufferedImage inImage) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(inImage, "jpg", bos );
        return bos.toByteArray();
    }
}
