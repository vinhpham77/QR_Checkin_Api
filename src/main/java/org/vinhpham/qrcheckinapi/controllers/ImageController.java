package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vinhpham.qrcheckinapi.dtos.Failure;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.services.ImageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null) {
            return Failure.response("error.not.found", HttpStatus.BAD_REQUEST);
        }

        String url = imageService.upload(file);
        return new ResponseEntity<>(url, HttpStatus.OK);
    }

    @GetMapping(value = "/{imageName}")
    public ResponseEntity<?> getImage(@PathVariable String imageName) {
        InputStreamResource resource = imageService.getImage(imageName);

        return Success.ok(resource);
    }

    @PostMapping("/by-content")
    public ResponseEntity<?> saveByContent(@RequestBody String content) {
        imageService.saveImagesInContent(content);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/by-content")
    public ResponseEntity<?> deleteByContent(@RequestBody String content) {
        imageService.removeImagesInContent(content);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
