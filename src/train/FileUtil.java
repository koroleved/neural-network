package train;

import data.Data;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class FileUtil {
    private final static int LABEL_FILE_MAGIC_INT = 2049;
    private final static int IMG_FILE_MAGIC_INT = 2051;

    public static List<DigitData> loadImageData(String filePrefix) {
        List<DigitData> images = null;
        ClassLoader loader = FileUtil.class.getClassLoader();
        String imgFileName = filePrefix + "-images-idx3-ubyte";
        String lblFileName = filePrefix + "-labels-idx1-ubyte";
        try (
                DataInputStream imageIS = new DataInputStream(loader.getResourceAsStream(imgFileName));
                DataInputStream labelIS = new DataInputStream(loader.getResourceAsStream(lblFileName));
        ) {

            if (imageIS.readInt() != IMG_FILE_MAGIC_INT)
                throw new IOException("Unknown file format for " + imgFileName);

            if (labelIS.readInt() != LABEL_FILE_MAGIC_INT)
                throw new IOException("Unknown file format for " + lblFileName);

            int nImages = imageIS.readInt();//количество картинок
            int nLabels = labelIS.readInt();//количество меток

            if (nImages != nLabels)
                throw new IOException(format("File %s and %s contains data for different number of images", imgFileName, lblFileName));//совпадает ли количество

            images = new ArrayList<>(nImages);

            int rows = imageIS.readInt();//28
            int cols = imageIS.readInt();//28

            byte[] data = new byte[rows * cols];

            //маштабируем значения пикселей
            for (int i = 0; i < nImages; i++) {
                double[] img = new double[rows * cols];
                //noinspection ResultOfMethodCallIgnored
                imageIS.read(data, 0, data.length);
                for (int d = 0; d < img.length; d++)
                    img[d] = (data[d] & 255) / 255.0;

                images.add(new DigitData(img, labelIS.readByte()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return images;
    }

    public static List<DigitData> loadImageDataNumbers(String path) throws IOException {
        List<DigitData> images = new ArrayList<>();
        Data data = new Data();
        List<File> imageName = data.getArrFile(path);//считываем имена
        List<BufferedImage> listBufferedImage = new ArrayList<>();
        for (File a : imageName
        ) {
            listBufferedImage.add(data.getBufferedImage(a)); //по именам порлучаем буферы
        }
        int i = 0;
        for (BufferedImage b : listBufferedImage
        ) {

            String str = (String) imageName.get(i).toString();
            String strNumber = str.substring(str.lastIndexOf('\\') + 1, str.lastIndexOf('_'));
            //imageName.toString() = imageName.toString().substring(imageName.toString().lastIndexOf('\\') + 1,imageName.toString().lastIndexOf('.'));
            images.add(new DigitData(Data.createArrayfromImage(b), Integer.parseInt(strNumber)));
            i++;
        }
        return images;
    }
}
