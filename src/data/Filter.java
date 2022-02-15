package data;

import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//получение линии, слова и символы на идеальном изображении без использолвания библиотек
public class Filter implements IFilter {

    @Override
    public List<BufferedImage> getLine(BufferedImage Image) throws IOException {
        int height1 = 0;
        int height2 = 0;
        int count = 1;
        Filter f = new Filter();
        List<BufferedImage> listLine = new ArrayList<>();
        for (int y = 1; y < Image.getHeight(); y++) {
            count = 0;
            for (int x = 1; x < Image.getWidth(); x++) {
                int rgb = Image.getRGB(x, y);
                if (rgb != -1) {
                    count++;
                    if (count > Image.getWidth() / 10 && height1 == 0) {
                        height1 = y;
                        break;
                    }
                }
            }
            if (count < Image.getWidth() / 10 && height1 != 0) {
                height2 = y;
            }
            if (height1 != 0 && height2 != 0 && (height2 - height1) > 7) {
                BufferedImage image1;
                image1 = f.getSubBufferedImage(Image, 0, height1, Image.getWidth(), height2 - height1);
                showImage(image1, "Поиск строк");
                listLine.add(image1);
                height1 = 0;
            }
        }
        return listLine;
    }

    public static void showImage(BufferedImage im, String title) {
        if (im == null) return;
        int w = 1000, h = 600;
        JFrame window = new JFrame(title);
        window.setSize(w, h);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon imageIcon = new ImageIcon(im);
        JLabel label = new JLabel(imageIcon);
        JScrollPane pane = new JScrollPane(label);
        window.setContentPane(pane);
        if (im.getWidth() < w && im.getHeight() < h) {
            window.pack();
        }
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
    @Override
    public List<BufferedImage> getWord(BufferedImage Image) throws IOException {
        int Width1 = 0;
        int Width2 = 0;
        List<BufferedImage> listWord = new ArrayList<>();
        Filter f = new Filter();
        for (int x = 0; x < Image.getWidth(); x++) {
            int count = 0;
            for (int y = 0; y < Image.getHeight(); y++) {
                int rgb = Image.getRGB(x, y);
                if (rgb != -1) {
                    count++;
                    if (count > 1 && Width1 == 0) {
                        Width1 = x;
                        break;
                    }
                }
            }
            if (count < 1 && Width1 != 0) {
                Width2 = x;
            }
            if ((Width1 != 0 || Width2 != 0) && (Width2 - Width1) > 14) {
                BufferedImage image1;
                image1 = f.getSubBufferedImage(Image, Width1, 0, Width2 - Width1, Image.getHeight());
                listWord.add(image1);
                Width1 = 0;
                Width2 = 0;
            }
        }
        return listWord;
    }

    @Override
    public List<BufferedImage> getSymbol(BufferedImage Image) throws IOException {
        int count1 = 0;
        int Width1 = 0;
        int Width2 = 0;
        Filter f = new Filter();
        List<BufferedImage> listSymbol = new ArrayList<>();
        for (int x = 0; x < Image.getWidth(); x++) {
            int count = 0;
            for (int y = 0; y < Image.getHeight(); y++) {
                int rgb = Image.getRGB(x, y);
                if (rgb != -1) {
                    count++;
                    if (count > Image.getHeight() / 2 && Width1 == 0) {
                        Width1 = x;
                        break;
                    }
                }
            }
            if (count < Image.getHeight() / 2 && Width1 != 0) {
                Width2 = x;
            }
            if (/*(Width1 != 0 || Width2 != 0) && */(Width2 - Width1) > 12) {
                if ((Width1 != 0) || (Width1 != 1) || (Width1 != 2)) {
                    Width1 = Width1 - 1;
                }
                BufferedImage image2 = f.getSubBufferedImage(Image, Width1, 0, Width2 - Width1 + 1, Image.getHeight());
                listSymbol.add(resizeBufferedImage(image2));
                count1++;
                Width1 = 0;
                Width2 = 0;
            }
        }
        return listSymbol;
    }

    @Override
    public void createBWImage(String pathImage, String pathBWImage) throws IOException {
        File dir = new File(pathImage);
        if (dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                File file = new File(pathImage + "//" + item.getName());
                BufferedImage inImage = ImageIO.read(file);
                BufferedImage image = new BufferedImage(inImage.getWidth(), inImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
                Graphics2D graphics = image.createGraphics();
                graphics.drawImage(inImage, 0, 0, null);
                writeImage(image,pathBWImage + "//" + item.getName());
            }
        }
    }


    @Override
    public BufferedImage resizeBufferedImage(BufferedImage originalImage, int newWIDTH, int newHEIGHT) {
        BufferedImage resizedImage = new BufferedImage(newWIDTH, newHEIGHT, BufferedImage.TYPE_INT_RGB);//захардкодил размер
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWIDTH, newHEIGHT, null);
        g.dispose();
        return resizedImage;
    }

    @Override
    public BufferedImage resizeBufferedImage(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);//захардкодил размер
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 28, 28, null);
        g.dispose();
        return resizedImage;
    }

    @Override
    public void writeImage(BufferedImage bufferedImage, String path) throws IOException {
        ImageIO.write(bufferedImage, "jpg", new File(path));
    }

    @Override
    public BufferedImage getSubBufferedImage(BufferedImage bufferedImageimage, int startX, int startY, int width, int height) throws IOException {
        BufferedImage inStr = bufferedImageimage.getSubimage(startX, startY, width, height);
        BufferedImage outStr = new BufferedImage(bufferedImageimage.getWidth(), bufferedImageimage.getHeight(), BufferedImage.TYPE_INT_RGB);//делаем заготовку поля для копирования
        Graphics graphics = outStr.createGraphics();
        graphics.drawImage(bufferedImageimage, 0, 0, null);//перерисовываем данные из исходной картинки в нашу заготовку
        return outStr;
    }

    public static void main(String[] args) throws IOException {
     Filter fm=new Filter();
     File file = new File("C:\\Users\\dennn\\PycharmProjects\\untitled2\\output.jpg");
     BufferedImage bf=ImageIO.read(file);
     fm.getLine(bf);
    }
}
