package com.hillel.items_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryControllerDto {
    private Long id;
    @NotEmpty(message = "Category name has to be not empty")
    @Size(min = 3, max = 50, message = "Category name has to be between 3 and 50 symbols")
    private String name;
    @NotNull(message = "Subcategories has to be not null")
    private List<@Valid SubcategoryControllerDto> subcategories;
}
