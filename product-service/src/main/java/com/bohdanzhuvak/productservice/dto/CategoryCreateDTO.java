package com.bohdanzhuvak.productservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryCreateDTO {
  private String name;
}
