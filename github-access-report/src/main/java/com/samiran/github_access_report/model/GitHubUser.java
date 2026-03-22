package com.samiran.github_access_report.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubUser {

    private Long id;
    private String login;

    private Permissions permissions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Permissions {

        private boolean admin;
        private boolean maintain;
        private boolean push;
        private boolean triage;
        private boolean pull;

        public String highestRole() {
            if (admin)    return "admin";
            if (maintain) return "maintain";
            if (push)     return "write";
            if (triage)   return "triage";
            return "read";
        }
    }
}
