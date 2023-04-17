package net.siisise.setup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 設定を保存するなにか.
 * JSONはSoftLibJSONが必要なので未対応.
 */
public class ProfSpool {

    /**
     * ユーザーhomeの指定名フォルダを返す.
     * @param folderName
     * @return 
     */
    private static File path(String folderName) {
        String homeval = System.getProperty("user.home");
        File home = new File(homeval);
        return new File(home, folderName);
    }

    /**
     * 設定ファイル的なものがあればFileを取得する.
     * @param folderName
     * @param fileName
     * @return 
     * @throws java.io.FileNotFoundException ファイルがない
     */
    public static File load(String folderName, String fileName) throws FileNotFoundException {
        File path = path(folderName);
        File file = new File(path, fileName);
        if ( file.isFile() ) {
            return file;
        }
        throw new java.io.FileNotFoundException();
    }
    
    public static File save(String folderName, String fileName) throws IOException {
        File path = path(folderName);
        if (!path.exists()) {
            // 多階層にはしない
            path.mkdirs();
        }
        if (!path.isDirectory()) {
            // ファイルなので不可かもしれない
            throw new IOException();
        }
        return new File(path, fileName);
    }
    
    public static void save(String folderName, String fileName, byte[] data) throws IOException {
        File tgFile = save(folderName, fileName);
        File newFile = save(folderName, fileName + ".new");
        File oldFile = save(folderName, fileName + ".old");
        if ( newFile.isFile() ) {
            newFile.delete();
        }
        try (FileOutputStream out = new FileOutputStream(newFile)) {
            out.write(data);
            out.flush();
            out.close();
            if ( tgFile.isFile() ) {
                if ( oldFile.isFile() ) {
                    oldFile.delete();
                }
                tgFile.renameTo(oldFile);
            }
            newFile.renameTo(tgFile);
            if ( tgFile.isFile() && oldFile.isFile() ) {
                oldFile.delete();
            }
        }
    }
    
}
