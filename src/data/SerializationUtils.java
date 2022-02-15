package data;

import java.io.*;

public class SerializationUtils {

    /**
     * Serialize the given object to the file
     *
     * @param obj
     * @param fileName
     * @throws java.io.IOException
     */
    public static void serialize(Object obj, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        }
    }

    /**
     * Deserialize to an object from the file
     * @param fileName
     * @return 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public static Object deserialize(String fileName)
            throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            return obj;
        }
    }
}
