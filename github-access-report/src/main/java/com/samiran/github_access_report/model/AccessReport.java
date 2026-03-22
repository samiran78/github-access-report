package com.samiran.github_access_report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessReport {

    private String organization;
    private int totalRepositories;
    private int totalUsers;
    private Instant generatedAt;

    private Map<String, List<RepoAccess>> userAccessMap;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepoAccess {

        private String repoName;
        private String repoFullName;
        private String repoUrl;
        private boolean privateRepo;
        private String permission;
    }
}