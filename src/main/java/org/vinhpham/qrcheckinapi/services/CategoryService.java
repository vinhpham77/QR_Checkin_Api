package org.vinhpham.qrcheckinapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.entities.Category;
import org.vinhpham.qrcheckinapi.repositories.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> get() {
        return categoryRepository.findAll();
    }
}
