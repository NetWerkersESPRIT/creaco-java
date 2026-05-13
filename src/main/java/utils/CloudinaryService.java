package utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class CloudinaryService {
    private static Cloudinary cloudinary;

    static {
        Properties properties = new Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream("config.properties")) {
            properties.load(fis);
            String cloudinaryUrl = properties.getProperty("CLOUDINARY_URL");
            if (cloudinaryUrl != null) {
                if (cloudinaryUrl.contains("**********")) {
                    System.err.println("[CloudinaryService] WARNING: CLOUDINARY_URL contains a placeholder '**********'. Please update config.properties with your real API secret.");
                }
                cloudinary = new Cloudinary(cloudinaryUrl);
            } else {
                System.err.println("CLOUDINARY_URL not found in config.properties");
            }
        } catch (java.io.IOException e) {
            System.err.println("Error initializing Cloudinary from config.properties: " + e.getMessage());
        }
    }

    public static String uploadFile(File file) throws IOException {
        if (cloudinary == null) {
            throw new IOException("Cloudinary is not initialized. Check your config.properties file.");
        }
        try {
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Invalid Signature")) {
                throw new IOException("Invalid Cloudinary Signature. Please check your API Secret in config.properties (it looks like you are still using the '**********' placeholder).", e);
            }
            throw new IOException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }
}
