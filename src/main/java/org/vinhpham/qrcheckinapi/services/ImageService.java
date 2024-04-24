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
import org.vinhpham.qrcheckinapi.utils.ConvertUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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

    public void saveImagesInContent(String content) {
        List<Integer> ids = regexIdInContext(content);
        System.out.println(ids.size());
        if (ids.isEmpty())
            return;

        try {
            List<Image> images = imageRepository.findByIdIn(ids);
            images.forEach(image -> image.setStatus(true));
            imageRepository.saveAll(images);
        } catch (Exception e) {
            throw HandleException.bad("error.image.not-exist");
        }
    }

    public void removeImagesInContent(String content) {
        List<Integer> ids = regexIdInContext(content);
        System.out.println(ids.size());
        if (ids.isEmpty())
            return;
        try {
            List<Image> images = imageRepository.findByIdIn(ids);
            images.forEach(image -> image.setStatus(false));
            imageRepository.saveAll(images);
        } catch (Exception e) {
            throw HandleException.bad("error.image.not-exist");
        }
    }

    public List<Integer> regexIdInContext(String content) {
        String allowedExtensions = String.join("|", this.allowedExtensions);
        String regex = "\\(http://localhost:8888/api/images/(.*?)\\.(" + allowedExtensions + ")\\)";

        return Pattern.compile(regex)
                .matcher(content)
                .results()
                .map(result -> ConvertUtils.toInteger(result.group(1)))
                .filter(Objects::nonNull)
                .toList();
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
    public void saveByUrl(String url) {
        String imageId = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        String extension = url.substring(url.lastIndexOf(".") + 1);

        Image image = imageRepository.findByIdAndExtensionAndStatus(ConvertUtils.toLong(imageId), extension, false);

        if (image != null) {
            image.setStatus(true);
            imageRepository.save(image);
        }
    }
}