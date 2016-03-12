package org.ametro.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FileUtils {

    public static void writeAllText(File file, String content) throws IOException {
        PrintWriter writer = new PrintWriter(file);
        try{
            writer.print(content);
        }finally {
            writer.close();
        }
    }

    public static void writeAllBytes(File file, byte[] content) throws IOException {
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(file);
            writer.write(content);
        }finally {
            if(writer!=null)
                writer.close();
        }
    }

    public static String readAllText(File file) throws IOException {
        return readAllText(new FileInputStream(file));
    }

    public static String readAllText(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            inputStream.close();
        }
    }

    public static void delete(File file) throws IOException {
        if(!file.delete()){
            throw new IOException("Cannot delete from " + file.getAbsolutePath());
        }
    }

    public static boolean safeDelete(File file) {
        return file.delete();
    }

    public static void move(File localMapFileTmp, File localMapFile) throws IOException {
        if(localMapFile.exists())
            delete(localMapFile);
        if(!localMapFileTmp.renameTo(localMapFile)){
            throw new IOException("Cannot rename file to " + localMapFile.getAbsolutePath());
        }
    }

}
