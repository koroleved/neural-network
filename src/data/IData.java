package data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IData {

  public File getFile(String pathFile);
  public List<File> getArrFile(String folder);
  public BufferedImage getBufferedImage(File file) throws IOException;
}
