package com.ai.video.FacelessVideo.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessRunner {
    public static void run(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Failed to run command: " + e.getMessage());
        }
    }
}
