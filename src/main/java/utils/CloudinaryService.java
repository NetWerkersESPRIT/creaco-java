package utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryService {
    private static Cloudinary cloudinary;

    static {
        try {
            Dotenv dotenv = Dotenv.load();
            String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
            if (cloudinaryUrl != null) {
                cloudinary = new Cloudinary(cloudinaryUrl);
            } else {
                System.err.println("CLOUDINARY_URL not found in .env file");
            }
        } catch (Exception e) {
            System.err.println("Error initializing Cloudinary: " + e.getMessage());
        }
    }

    public static String uploadFile(File file) throws IOException {
        if (cloudinary == null) {
            throw new IOException("Cloudinary is not initialized. Check your .env file.");
        }
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }
}
