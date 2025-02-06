package com.travel.together.TravelTogether.album.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.album.dto.*;
import com.travel.together.TravelTogether.album.service.PhotoAlbumService;
import com.travel.together.TravelTogether.album.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/albums")
public class PhotoAlbumController {

    private final PhotoAlbumService photoAlbumService;
    private final PhotoService photoService;

    public PhotoAlbumController(PhotoAlbumService photoAlbumService, PhotoService photoService) {
        this.photoAlbumService = photoAlbumService;
        this.photoService = photoService;
    }

//    // [POST] /albums
//    // 새로운 앨범을 생성
//    @PostMapping
//    public ResponseEntity<PhotoAlbumResponseDto> createAlbum(@RequestBody PhotoAlbumRequestDto requestDto) {
//        PhotoAlbumResponseDto albumResponse = photoAlbumService.createPhotoAlbum(requestDto);
//        return ResponseEntity.ok(albumResponse);
//    }

    // [GET] /albums
    // 전체 앨범 목록 조회
    @GetMapping
    public ResponseEntity<List<PhotoAlbumResponseDto>> getAllAlbums() {
        List<PhotoAlbumResponseDto> albumList = photoAlbumService.getAllAlbums();
        return ResponseEntity.ok(albumList);
    }

    // [GET] /albums/{albumId}/photos
    // 특정 앨범의 사진 조회
    @GetMapping("/{albumId}/photos")
    public ResponseEntity<List<PhotoResponseDto>> getPhotosByAlbum(@PathVariable("albumId") int albumId) {
        List<PhotoResponseDto> photos = photoService.getPhotosByAlbumId(albumId);
        return ResponseEntity.ok(photos);
    }

    // [POST] /albums/{albumId}/photos
    // 특정 앨범에 사진 업로드
    @PostMapping("/{albumId}/photos")
    public ResponseEntity<List<PhotoResponseDto>> uploadPhotoToAlbum(
            @PathVariable("albumId") int albumId,
            @RequestPart(value = "photoData") String photoDataJson,
            @RequestPart("files") List<MultipartFile> files) {

        BatchPhotoRequestDto batchPhotoRequestDto = null;
        if (photoDataJson != null && !photoDataJson.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                batchPhotoRequestDto = objectMapper.readValue(photoDataJson, BatchPhotoRequestDto.class);
            } catch (Exception e) {
                throw new RuntimeException("Invalid JSON format", e);
            }
        }

        assert batchPhotoRequestDto != null;
        if (batchPhotoRequestDto.getPhotos().size() != files.size()) {
            throw new IllegalArgumentException("사진 데이터와 파일 개수가 일치하지 않습니다.");
        }

        List<PhotoResponseDto> responses = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            PhotoRequestDto photoDto = batchPhotoRequestDto.getPhotos().get(i);
            photoDto.setAlbumId(albumId);
            PhotoResponseDto response = photoService.uploadPhoto(photoDto, files.get(i));
            responses.add(response);
        }
        return ResponseEntity.ok(responses);
    }

    // [DELETE] /albums/{albumId}/photos/{photoId}
    // 특정 사진 삭제
    @DeleteMapping("/{albumId}/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable int albumId,
            @PathVariable Integer photoId
    ) {
        photoService.deletePhoto(albumId, photoId);
        return ResponseEntity.noContent().build();
    }
}