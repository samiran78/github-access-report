package com.samiran.github_access_report.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data // Lombok generates getters, setters
//to exactly working with only needed fields we need jsonIgnorecase
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepo {
    private Long id;
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("private")
    private boolean privateRepo;

}
