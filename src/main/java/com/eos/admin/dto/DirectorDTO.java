package com.eos.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDTO {
    private Long id;
    private String directorName;
    private String directorDesignation;
    private List<AddressDTO> address;
    private String directorPhone;
}