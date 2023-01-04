package com.example.grocery.webApi.responses.employee;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetAllEmployeeResponse {

    private int id;

    private String email;

    private String password;

    private String firstName; // challenge= full name yapıp ikisini de almaya çalış...

    private String lastName;

    private String nationalIdentity;

    private LocalDate yearOfBirth;

    private double salary;
}