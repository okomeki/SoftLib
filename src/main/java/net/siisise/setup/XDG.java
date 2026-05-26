/*
 * Copyright 2026 okome.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.siisise.setup;

import java.io.File;

/**
 * XDG に WindowsのAppDataを混ぜてみる
 * 
 */
public class XDG {
    String app;
    
    public XDG(String name) {
        app = name;
    }

    /**
     * Javaのpropertyから取得.
     * 環境変数 HOME または USERPROFILE を見てもいいのか
     * @return 
     */
    public static File home() {
        String home = System.getProperty("user.home");
        return new File(home);
    }
    
    public File etc() {
        return new File("/etc/" + app);
    }

    /**
     * $XDG_DATA_HOME ユーザ固有データ
     * @return 
     */
    public static File dataHome(String subPath) {
        return path("XDG_DATA_HOME", ".local/share", subPath);
    }

    public File data() {
        return dataHome(app);
    }

    /**
     * $XDG_CONFIG_HOME ユーザ固有の設定ファイル
     * /etc/ 相当
     * @return 
     */
    public static File configHome(String subPath) {
        return path("XDG_CONFIG_HOME", ".config", subPath);
    }
    
    public File config() {
        return configHome(app);
    }

    /**
     * $XDG_STATE_HOME ユーザ固有の状態データ 再起動でも保持される
     * @return 
     */
    public static File stateHome(String subPath) {
        return path("XDG_STATE_HOME", ".local/state", subPath);
    }

    public File state() {
        return stateHome(app);
    }

    /**
     * ユーザ固有の実行可能ファイル
     * @return 
     */
    public static File bin(String subPath) {
//        String configPath = System.getenv("XDG_STATE_HOME");
        File config;
//        if (configPath == null) {
            config = new File(home(), ".local/bin");
//        } else {
//            config = new File(configPath);
//        }
        return new File(config, subPath);
    }

    /**
     * $XDG_DATA_DIRS ユーザ固有のデータファイルの検索対象
     * @return 
     */
    public static File[] dataDirs(String subPath) {
        return dirs("XDG_DATA_DIRS","/usr/local/share/:/usr/share/", subPath);
    }

    /**
     * $XDG_CONFIG_DIRS ユーザ固有の設定ファイルの検索対象
     * @return 
     */
    public static File[] configDirs(String subPath) {
        return dirs("XDG_CONFIG_DIRS","/etc/xdg", subPath);
    }

    /**
     * $XDG_CACHE_HOME ユーザ固有の非必須(キャッシュ)データの書き込み
     * /var/cache
     * Windows TEMP など
     * @return 
     */
    public static File cacheHome(String subPath) {
        return path("XDG_CACHE_HOME", ".cache", subPath);
    }

    public File cache() {
        return cacheHome(app);
    }

    /**
     * 
     * @param name
     * @param base
     * @param subPath
     * @return 
     */
    private static File path(String name, String base, String subPath) {
        String path = System.getenv(name);
        File file;
        if (path == null) {
            file = new File(home(), base);
        } else {
            file = new File(path);
        }
        return new File(file, subPath);
    }

    /**
     * 
     * @param name
     * @param path
     * @param subPath
     * @return 
     */
    private static File[] dirs(String name, String path, String subPath) {
        String configPaths = System.getenv(name);
        File[] config;
        String pathSeparator = System.getProperty("path.separator");
        if (configPaths != null) {
            path = configPaths;
        }
        String[] paths = path.split(pathSeparator);
        config = new File[paths.length];
        for ( int i = 0; i < paths.length; i++ ) {
            config[i] = new File(new File(paths[i]), subPath);
        }
        return config;
    }
}
