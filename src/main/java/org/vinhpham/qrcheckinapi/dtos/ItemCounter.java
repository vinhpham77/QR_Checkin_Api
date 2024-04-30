package org.vinhpham.qrcheckinapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemCounter<T> {
    List<T> items;
    long counter;
}
