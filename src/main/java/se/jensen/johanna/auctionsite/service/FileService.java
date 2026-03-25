package se.jensen.johanna.auctionsite.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  private final String uploadDir = "C:/Users/johannajonsson/auction-images/";

  public String saveFile(MultipartFile file) {
    if (file.isEmpty()) {
      return null;
    }

    try {
      Path root = Paths.get(uploadDir);
      if (!Files.exists(root)) {
        Files.createDirectories(root);
      }
      String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
      Path destination = root.resolve(fileName);
      file.transferTo(destination);
      return fileName;
    } catch (IOException e) {
      throw new RuntimeException(String.format("Failed to save file: %s", e.getMessage()), e);
    }
  }


}
