package com.sdk;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] argv) throws IOException {
        System.out.println("Main");
        File dir = new File(new File("").getAbsolutePath());
        if (dir.exists() && dir.isDirectory()) {
            File[] listDir = dir.listFiles();
            for (File file : listDir != null ? listDir : new File[0]) {
                if (file.exists() && file.isDirectory()) {
                    File[] moduleDir = file.listFiles();
                    for (File file2 : moduleDir != null ? moduleDir : new File[0]) {
                        if (file2.isDirectory() && file2.getName().equals("src")) {
                            File[] srcDir = file2.listFiles();
                            for (File file3 : srcDir != null ? srcDir : new File[0]) {
                                if (file3.isDirectory() && file3.getName().equals("test") || file3.getName().equals("androidTest")) {
                                    System.out.println(file3.getAbsolutePath());
                                    deleteDir(file3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void deleteDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.getName().equals(".") || file.getName().equals("..")) {
                continue;
            }
            if (file.isFile()) {
                file.delete();
            }
            if (file.isDirectory()) {
                deleteDir(file);
            }
        }
        dir.delete();
    }
}
