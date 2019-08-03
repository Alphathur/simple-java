package com.alphathur.filetools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSearcher {

    public static void main(String[] args) throws IOException {
        List<File> files = getFileList ( "D:\\", "mp4", "flv", "avi" );
        System.out.println ( "文件数量：" + files.size () + ",文件路径：" );
        for (File f : files) {
            System.out.println ( f.getAbsolutePath () );
        }
    }

    /**
     * 递归查询特定格式的所有文件
     *
     * @param strPath
     * @param regex
     * @return
     */
    public static List<File> getFileList(String strPath, String... regex) {
        List<File> filelist = new ArrayList<> ();
        File dir = new File ( strPath );
        File[] files = dir.listFiles ();
        if (files == null || files.length == 0) {
            return filelist;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory ()) { // 判断是文件还是文件夹
                List<File> fls = getFileList ( files[i].getAbsolutePath (), regex ); // 获取文件绝对路径
                filelist.addAll ( fls );
            } else if (isRightFile ( files[i].getName (), regex )) { // 判断文件名是否以regex结尾
                filelist.add ( files[i] );
            }
        }
        return filelist;
    }

    private static boolean isRightFile(String fileName, String... regex) {
        for (String reg :regex) {
            if (fileName.endsWith (reg)) {
                return true;
            }
        }
        return false;
    }
}
