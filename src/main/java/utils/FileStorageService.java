package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileStorageService {
    private static final String UPLOAD_DIR = "uploads";

    static {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Copies the selected file to the local uploads directory.
     * @param sourceFile The file selected by the user.
     * @param subDir Optional subdirectory (e.g. "courses", "resources").
     * @return The path to the stored file.
     * @throws IOException If the copy operation fails.
     */
    public static String storeFile(File sourceFile, String subDir) throws IOException {
        Path targetDir = Paths.get(UPLOAD_DIR, subDir != null ? subDir : "");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        String fileName = UUID.randomUUID().toString() + "_" + sourceFile.getName();
        Path targetPath = targetDir.resolve(fileName);

        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return the absolute path for the ImageView to load it easily
        // Or return a file URI
        return targetPath.toFile().toURI().toString();
    }
}
