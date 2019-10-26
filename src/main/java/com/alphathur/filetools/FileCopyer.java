package com.alphathur.filetools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileCopyer {

    public static void main(String[] args) throws InterruptedException, IOException {
        List<File> files = FileSearcher.getFileList ( "G:\\BaiduNetdiskDownload\\MAIN MENU RANDOMIZER ver 3dot31", "dds" );
        copyAll ( files, "H:\\test_files" );
    }


    /**
     * 使用多线程将一批文件复制到指定文件夹下
     *
     * @param files
     * @param targetUrl
     */
    public static void copyAll(List<File> files, String targetUrl) throws InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool ( Runtime.getRuntime ().availableProcessors () * 2 );
        Set<String> fileNameFilter = new HashSet<> ();
        files.stream ().forEach ( sourceFile -> {
//            String fileName = getFileNameByFilter ( fileNameFilter, sourceFile.getName () );
            String fileName = getTimeFileNameByFilter ( sourceFile.getName () );
            File targetFile = new File ( targetUrl + "\\" + fileName );
            Runnable runnable = new FileRunnable ( sourceFile.toPath (), targetFile.toPath () );
            exec.execute ( runnable );
        } );
        exec.shutdown ();
        while (!exec.awaitTermination ( 1, TimeUnit.SECONDS )) ;
        System.out.println ( "Copy Done" );
    }

    /**
     * generate a new filename based on source filename, if file names are duplicated , then generate new file name by add number
     *
     * @param fileNameFilter
     * @param fileName
     * @return
     */
    private static String getFileNameByFilter(Set<String> fileNameFilter, String fileName) {
        boolean exist = !fileNameFilter.add ( fileName );
        int i = 0;
        while (exist) {
            String extensionName = getExtensionName ( fileName );
            String tmp = fileName.substring ( 0, fileName.lastIndexOf ( extensionName ) - 1 );
            fileName = tmp + "(" + i + ")" + "." + extensionName;
            exist = !fileNameFilter.add ( fileName );
            i++;
        }
        return fileName;
    }

    /**
     * generate a new filename with System.nanoTime(). and ignore the sourc filename
     *
     * @param fileName
     * @return
     */
    private static String getTimeFileNameByFilter(String fileName) {
        String extensionName = getExtensionName ( fileName );
        return System.nanoTime () + "." + extensionName;
    }


    /**
     * 获取文件扩展名
     *
     * @param filename
     * @return
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length () > 0)) {
            int dot = filename.lastIndexOf ( '.' );
            if ((dot > -1) && (dot < (filename.length () - 1))) {
                return filename.substring ( dot + 1 );
            }
        }
        return filename;
    }

    /**
     * 定义文件拷贝线程，返回处理结果
     */
    private static class FileRunnable implements Runnable {

        private Path sourcePath;
        private Path targetPath;

        public FileRunnable(Path sourcePath, Path targetPath) {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
        }

        @Override
        public void run() {
            Path path = null;
            try {
                path = Files.copy ( sourcePath, targetPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );
            } catch (IOException e) {
                e.printStackTrace ();
            }
            if (path == null) {
                System.out.println ( "copy fail:" + sourcePath.toString () );
            }
        }
    }
}
