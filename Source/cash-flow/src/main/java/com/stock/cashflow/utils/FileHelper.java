package com.stock.cashflow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileHelper {

    private static final Logger log = LoggerFactory.getLogger(FileHelper.class);


    public static List<String> getAllXLSXFiles(String folderPath) {
        List<String> xlsxFiles = new ArrayList<>();

        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".xlsx")) {
                    xlsxFiles.add(file.getAbsolutePath());
                }
            }
        } else {
            log.info("Folder not found: {}", folderPath);
        }

        return xlsxFiles;
    }

    public static boolean fileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path) && Files.isRegularFile(path);
    }

}
