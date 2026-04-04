package se.jensen.johanna.auctionsite.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.jensen.johanna.auctionsite.exception.ImageUploadException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

  @Value("${aws.s3.bucket-name}")
  private String bucketName;

  @Value("${aws.s3.region}")
  private String region;

  private final S3Client s3Client;

  public String uploadImage(MultipartFile file) {
    String fileName = UUID.randomUUID() + ".jpg";
    try {
      byte[] resizedImage = resizeAndCompress(file);
      PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName)
          .contentType("image/jpeg").build();
      s3Client.putObject(request, RequestBody.fromBytes(resizedImage));
      String imageUrl = buildImageUrl(fileName);
      log.info("Image with url: {} uploaded to S3", imageUrl);
      return imageUrl;
    } catch (IOException e) {
      log.error("Error uploading image to S3: {}", e.getMessage());
      throw new ImageUploadException("Failed to upload image to S3", e);
    }
  }

  private byte[] resizeAndCompress(MultipartFile file) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Thumbnails.of(file.getInputStream()).size(1280, 1280).outputFormat("jpg").outputQuality(0.85)
        .toOutputStream(outputStream);

    return outputStream.toByteArray();
  }

  private String buildImageUrl(String fileName) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
  }

  public void deleteImage(String imageUrl) {
    if (imageUrl == null || imageUrl.isEmpty()) {
      return;
    }
    try {
      String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
      s3Client.deleteObject(b -> b.bucket(bucketName).key(fileName));
    } catch (Exception e) {
      log.error("Error deleting image with url:{}, message: {}", imageUrl, e.getMessage());
    }
  }
}
