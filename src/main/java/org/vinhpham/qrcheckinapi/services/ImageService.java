package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.entities.Image;
import org.vinhpham.qrcheckinapi.repositories.ImageRepository;
import org.vinhpham.qrcheckinapi.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);
    private final ImageRepository imageRepository;

    @Value("${uploads.path}")
    private String uploadPath;

    @Value("#{'${upload.extensions.allowed}'.split(',')}")
    private List<String> allowedExtensions;

    public String upload(MultipartFile file) {
        String extension = getFileExtension(file);

        if (!allowedExtensions.contains(extension)) {
            throw new HandleException("error.image.invalid", HttpStatus.BAD_REQUEST);
        }

        try {
            Image image = Image.builder()
                    .extension(extension)
                    .status(false)
                    .build();
            image = imageRepository.save(image);

            Files.createDirectories(Paths.get(uploadPath));
            Path path = Paths.get(uploadPath + image.getId() + '.' + image.getExtension());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return image.getId().toString() + '.' + image.getExtension();
        } catch (Exception e) {
            LOGGER.error("Exception", e);
            throw new HandleException("error.something.wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null) {
            throw new HandleException("error.extension.not-found", HttpStatus.BAD_REQUEST);
        }

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0) {
            throw new HandleException("error.extension.not-found", HttpStatus.BAD_REQUEST);
        }

        return originalFilename.substring(dotIndex + 1);
    }

    public InputStreamResource getImage(String imageName) {
        try {
            Path imagePath = Paths.get(uploadPath + imageName);
            return new InputStreamResource(Files.newInputStream(imagePath));
        } catch (IOException e) {
            throw new HandleException("error.something.wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void cleanupImages() {
        try {
            Date now = new Date();
            Date twoHoursBefore = new Date(now.getTime() - 24 * 60 * 60 * 1000);
            List<Image> images = imageRepository.findImagesByStatusAndCreatedAt(false, twoHoursBefore);

            for (Image i : images) {
                Path pathImage = Paths.get(uploadPath + i.getId() + "." + i.getExtension());
                Files.delete(pathImage);
            }

            imageRepository.deleteAll(images);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
    }

    @Transactional
    public void saveByName(String imageName) {
        String imageId = imageName.substring(0, imageName.lastIndexOf("."));
        String extension = imageName.substring(imageName.lastIndexOf(".") + 1);

        Image image = imageRepository.findByIdAndExtensionAndStatus(Utils.toLong(imageId), extension, false);

        if (image != null) {
            image.setStatus(true);
            imageRepository.save(image);
        }
    }

    public void deleteByName(String imageName) {
        String imageId = imageName.substring(0, imageName.lastIndexOf("."));
        String extension = imageName.substring(imageName.lastIndexOf(".") + 1);

        deleteImageByName(imageId, extension);
    }

    private void deleteImageByName(String imageId, String extension) {
        Image image = imageRepository.findByIdAndExtensionAndStatus(Utils.toLong(imageId), extension, true);

        if (image != null) {
            imageRepository.delete(image);
            Path pathImage = Paths.get(uploadPath + image.getId() + "." + image.getExtension());

            try {
                Files.delete(pathImage);
            } catch (Exception e) {
                LOGGER.error("Exception", e);
            }
        }
    }
}
