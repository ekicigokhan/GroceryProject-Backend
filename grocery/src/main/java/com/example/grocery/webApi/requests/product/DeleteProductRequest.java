package com.example.grocery.webApi.requests.product;

import jakarta.validation.constraints.Positive;

// import javax.validation.constraints.NotBlank;
// import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeleteProductRequest {

    @Positive
    private int id;
}