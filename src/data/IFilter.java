package data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public interface IFilter {

    List<BufferedImage> getLine(BufferedImage Image) throws IOException;

    List<BufferedImage> getWord(BufferedImage Image) throws IOException;

    List<BufferedImage> getSymbol(BufferedImage Image) throws IOException;

    void createBWImage(String pathImage,String pathBWImage) throws IOException;

    BufferedImage resizeBufferedImage(BufferedImage originalImage, int newWIDTH, int newHEIGHT);

    BufferedImage resizeBufferedImage(BufferedImage originalImage);

    void writeImage(BufferedImage bufferedImage, String path) throws IOException;

    BufferedImage getSubBufferedImage(BufferedImage bufferedImageimage, int startX, int startY, int width, int height) throws IOException;



}
