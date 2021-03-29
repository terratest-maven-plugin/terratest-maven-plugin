package com.github.terratest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public static void writeToFile(List<String> content, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            for (String str : content) {
                writer.write(str + System.lineSeparator());
            }
        } catch (IOException e) {
            LOGGER.error("Can't save result to logfile: ", e);
        }
    }
}
