package com.aau.moodle20.payload.request;

import javax.validation.constraints.NotBlank;

public class CreateExampleRequest {

    private Long exampleSheetId;
    private String name;
    private String description;
    private Integer minKreuzel;
    private Integer minPoints;


}
