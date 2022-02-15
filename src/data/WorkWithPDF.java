package data;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WorkWithPDF {
    public void convertPDFToImage(String pathPDF, String pathForImage) throws IOException {
        File file = new File(pathPDF);
        PDDocument document = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(document);
        for (int i = 0; i < document.getNumberOfPages(); i++) {
            BufferedImage image = renderer.renderImage(i);
            ImageIO.write(image, "JPEG", new File(pathForImage + "output" + i+".jpg"));
        }
        document.close();
    }

    public static void main(String[] args) throws IOException {
        WorkWithPDF test=new WorkWithPDF();
        test.convertPDFToImage("F:\\Диплом\\Для обучения\\ВЖАЯ.430424.001ТУ.pdf","C:\\Users\\dennn\\Desktop\\neural-network\\test\\");

    }
}
