package top.wboost.common.spring.boot.swagger.util;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileCopyUtil {

    public static void loadRecourseFromJarByFolder(String targetolderPath, String toFolderPath) throws IOException {
        URL url = FileCopyUtil.class.getResource("/" + targetolderPath);
        URLConnection urlConnection = url.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            copyJarResources((JarURLConnection) urlConnection, targetolderPath, toFolderPath);
        } else {
            copyFileResources(url, targetolderPath, toFolderPath);
        }
    }

    /**
     * 当前运行环境资源文件是在文件里面的
     * @param url
     * @param toFolderPath
     * @throws IOException
     */
    private static void copyFileResources(URL url, String targetolderPath, String toFolderPath) throws IOException {
        File root = new File(url.getPath());
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    loadRecourseFromJarByFolder(targetolderPath + "/" + file.getName(), toFolderPath);
                } else {
                    loadRecourseFromJar(targetolderPath + "/" + file.getName(), targetolderPath, toFolderPath);
                }
            }
        }
    }

    /**
     * 当前运行环境资源文件是在jar里面的
     * @param jarURLConnection
     * @throws IOException
     */
    private static void copyJarResources(JarURLConnection jarURLConnection, String targetolderPath, String toFolderPath)
            throws IOException {
        JarFile jarFile = jarURLConnection.getJarFile();
        Enumeration<JarEntry> entrys = jarFile.entries();
        while (entrys.hasMoreElements()) {
            JarEntry entry = entrys.nextElement();
            if (entry.getName().startsWith(jarURLConnection.getEntryName()) && !entry.getName().endsWith("/")) {
                loadRecourseFromJar("/" + entry.getName(), targetolderPath, toFolderPath);
            }
        }
        jarFile.close();
    }

    public static void loadRecourseFromJar(String path, String targetolderPath, String toFolderPath)
            throws IOException {
        if (!path.startsWith("/")) {
            //throw new IllegalArgumentException("The path has to be absolute (start with '/').");
            path = "/" + path;
        }

        if (path.endsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (cat not end with '/').");
        }

        String now = path.replace("/" + targetolderPath + "/", "");
        String filename = toFolderPath + "/" + now;

        // If the folder does not exist yet, it will be created. If the folder
        // exists already, it will be ignored
        File dir = new File(filename.substring(0, filename.lastIndexOf("/")));
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // If the file does not exist yet, it will be created. If the file
        // exists already, it will be ignored
        System.out.println(filename);
        File file = new File(filename);

        if (!file.exists() && !file.createNewFile()) {
            System.out.println("create file :" + filename + " failed");
            return;
        }

        // Prepare buffer for data copying
        byte[] buffer = new byte[1024];
        int readBytes;

        // Open and check input stream
        URL url = FileCopyUtil.class.getResource(path);
        URLConnection urlConnection = url.openConnection();
        InputStream is = urlConnection.getInputStream();

        if (is == null) {
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        }

        // Open output stream and copy data between source file in JAR and the
        // temporary file
        OutputStream os = new FileOutputStream(file);
        try {
            while ((readBytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        } finally {
            // If read/write fails, close streams safely before throwing an
            // exception
            os.close();
            is.close();
        }

    }

    public static InputStream readJarFile(String path) throws IOException {
        if (!path.startsWith("/")) {
            //throw new IllegalArgumentException("The path has to be absolute (start with '/').");
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (cat not end with '/').");
        }
        URL url = FileCopyUtil.class.getResource(path);
        URLConnection urlConnection = url.openConnection();
        InputStream is = urlConnection.getInputStream();
        return is;
    }

}
