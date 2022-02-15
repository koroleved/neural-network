package data;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FilteOpenCV {
    public static final Scalar COLOR_WHITE = colorRGB(255, 255, 255);
    public static final Scalar COLOR_BLUE = colorRGB(0, 0, 255);
    public static Scalar colorRGB(double red, double green, double blue) {
        return new Scalar(blue, green, red);
    }
    //получаем BufferedImage
    public static BufferedImage MatToBufferedImage(Mat m) {
        if (m == null || m.empty()) return null;
        if (m.depth() == CvType.CV_8U) {
        } else if (m.depth() == CvType.CV_16U) { // CV_16U => CV_8U
            Mat m_16 = new Mat();
            m.convertTo(m_16, CvType.CV_8U, 255.0 / 65535);
            m = m_16;
        } else if (m.depth() == CvType.CV_32F) { // CV_32F => CV_8U
            Mat m_32 = new Mat();
            m.convertTo(m_32, CvType.CV_8U, 255);
            m = m_32;
        } else
            return null;
        int type = 0;
        if (m.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else if (m.channels() == 3)
            type = BufferedImage.TYPE_3BYTE_BGR;
        else if (m.channels() == 4)
            type = BufferedImage.TYPE_4BYTE_ABGR;
        else
            return null;
        byte[] buf = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, buf);
        byte tmp = 0;
        if (m.channels() == 4) { // BGRA => ABGR
            for (int i = 0; i < buf.length; i += 4) {
                tmp = buf[i + 3];
                buf[i + 3] = buf[i + 2];
                buf[i + 2] = buf[i + 1];
                buf[i + 1] = buf[i];
                buf[i] = tmp;
            }
        }
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        byte[] data =
                ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buf, 0, data, 0, buf.length);
        return image;
    }

    //показываем изображение
    public static void showImage(Mat img, String title) {
        BufferedImage im = MatToBufferedImage(img);
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


    public List<Mat> findCharacters(String str)
    {
        List<Mat> listChatacters=new ArrayList<>();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread("C:\\Users\\dennn\\Desktop\\021.PNG");//C //E:\neural-network\test\output1.png
        Mat masked = new Mat();
        img.copyTo( masked, img );
        if (img.empty()) {
            System.out.println("Не удалось загрузить изображение");
        }
        showImage(img, "Оригинал");
        Mat imgGray = new Mat();
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
        Mat edges = new Mat();
        Imgproc.Canny(imgGray, edges, 80, 200);
        Mat edgesCopy = edges.clone(); // Создаем копию
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(edgesCopy, contours, new Mat(),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0, j = contours.size(); i < j; i++) {
            System.out.println(Imgproc.contourArea(contours.get(i)));
            Rect r = Imgproc.boundingRect(contours.get(i));
            Mat cropped = masked.submat(r);
            System.out.println("boundingRect = " + r);
            double len = Imgproc.arcLength(
                    new MatOfPoint2f(contours.get(i).toArray()), true);
            System.out.println("arcLength = " + len);
            Imgproc.rectangle(img, new Point(r.x, r.y),
                    new Point(r.x + r.width - 1, r.y + r.height - 1),
                    COLOR_BLUE);
            // showImage(img, "1");
            listChatacters.add(cropped);
        }
        for (Mat b:listChatacters
        ) {
            showImage(b, "Поиск символов");
        }
        img.release();
        imgGray.release();
        edges.release();
        edgesCopy.release();
        return listChatacters;
    }
    public List<Mat>  findStrings(String str) {
        Mat Main = Imgcodecs.imread(str);//"C:\\Users\\dennn\\PycharmProjects\\untitled2\\imag\\output11.jpg");
        Mat masked = new Mat();
        Main.copyTo( masked, Main );
        Mat rgb = new Mat();
        List<Mat>listMat=new ArrayList<>();
        Imgproc.pyrDown(Main, rgb);
        Mat small = new Mat();
        Imgproc.cvtColor(rgb, small, Imgproc.COLOR_RGB2GRAY);
        Mat grad = new Mat();
        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Imgproc.morphologyEx(small, grad, Imgproc.MORPH_GRADIENT, morphKernel);
        Mat bw = new Mat();
        Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Mat connected = new Mat();
        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 1));
        Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE, morphKernel);
        Mat mask = Mat.zeros(bw.size(), CvType.CV_8UC1);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(connected, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        for (int idx = 0; idx < contours.size(); idx++) {
            Rect rect = Imgproc.boundingRect(contours.get(idx));
            Mat maskROI = new Mat(mask, rect);
            Imgproc.drawContours(mask, contours, idx, new Scalar(255, 255, 255), Core.FILLED);
            // maskROI.setTo(new Scalar(0, 0, 0));
            double r = (double) Core.countNonZero(maskROI) / (rect.width * rect.height);
            if (r > .45 && (rect.height > 8 && rect.width > 8)) {
                Imgproc.rectangle(rgb, rect.br(), new Point(rect.br().x - rect.width, rect.br().y - rect.height), new Scalar(0, 255, 0));
                Mat cropped = masked.submat(rect);
                listMat.add(masked);
            }
            String outputfile = "trovato.png";
            //  listMat.add(rgb);
            Imgcodecs.imwrite(outputfile, rgb);
        }
        for (Mat b:listMat
        ) {
            FilteOpenCV.showImage(b,"Поиск строк");
        }
        FilteOpenCV.showImage(rgb,"Поиск строк");
        return listMat;
    }
        public static void main (String[]args) {
            //imageToFindAframe();

            List<Mat> listChatacters = new ArrayList<>();
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Data data = new Data();
            List<File> listFile = data.getArrFile("C:\\Users\\dennn\\Desktop\\for_test\\007_5.PNG");
          //  for (File f : listFile
          //  ) {
            File file=new File("C:\\Users\\dennn\\Desktop\\for_test\\002_1.PNG2.png");
                Mat img = Imgcodecs.imread(file.toString());//C //E:\neural-network\test\output1.png
                Mat masked = new Mat();
                img.copyTo(masked, img);
                if (img.empty()) {
                    System.out.println("Не удалось загрузить изображение");
                    return;
                }
                showImage(img, "Оригинал");
                Mat imgGray = new Mat();
                Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
                Mat edges = new Mat();
                Imgproc.Canny(imgGray, edges, 80, 200);
                Mat edgesCopy = edges.clone(); // Создаем копию
                ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Imgproc.findContours(edgesCopy, contours, new Mat(),
                        Imgproc.RETR_EXTERNAL,
                        Imgproc.CHAIN_APPROX_SIMPLE);
                for (int i = 0, j = contours.size(); i < j; i++) {
                    System.out.println(Imgproc.contourArea(contours.get(i)));
                    Rect r = Imgproc.boundingRect(contours.get(i));
                    Mat cropped = masked.submat(r);//найденный контур передаем в маску
                    System.out.println("boundingRect = " + r);
                    double len = Imgproc.arcLength(
                            new MatOfPoint2f(contours.get(i).toArray()), true);
                    System.out.println("arcLength = " + len);
                    Imgproc.rectangle(img, new Point(r.x, r.y),
                            new Point(r.x + r.width - 1, r.y + r.height - 1),
                            COLOR_BLUE);
                    // showImage(img, "1");
                    listChatacters.add(cropped);
                    String outputfile = "C:\\Users\\dennn\\Desktop\\for_test\\"+file.getName()+i+".png";
                    //  listMat.add(rgb);
                    Imgcodecs.imwrite(outputfile, cropped);
                }
              /*  for (Mat b : listChatacters
                ) {
                    showImage(b, "Поиск символов");
                }*/
                showImage(img,"Поиск символов");
                img.release();
                imgGray.release();
                edges.release();
                edgesCopy.release();
            }
     //   }
    }

