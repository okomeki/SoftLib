package net.siisise.setup;

import java.io.File;
import java.io.IOException;

/**
 * 設定を保存するなにか
 */
public class ProfSpool {

    private File path(String folderName) {
        String homeval = System.getProperty("user.home");
        File home = new File(homeval);
        return new File(home, folderName);
    }

    public File load(String folderName, String fileName) {
        File path = path(folderName);
        return new File(path, fileName);
    }

    public File save(String folderName, String fileName) throws IOException {
        File path = path(folderName);
        if (!path.exists()) {
            // 多階層にはしない
            path.mkdir();
        }
        if (!path.isDirectory()) {
            // ファイルなので不可かもしれない
            throw new IOException();
        }
        return new File(path, fileName);
    }
}
