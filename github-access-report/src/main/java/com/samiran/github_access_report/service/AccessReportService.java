package com.samiran.github_access_report.service;

import com.samiran.github_access_report.client.GitHubApiClient;
import com.samiran.github_access_report.model.AccessReport;
import com.samiran.github_access_report.model.AccessReport.RepoAccess;
import com.samiran.github_access_report.model.GitHubRepo;
import com.samiran.github_access_report.model.GitHubUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessReportService {

    private final GitHubApiClient gitHubApiClient;
    private final ExecutorService gitHubExecutor; // injected from GitHubConfig

    public AccessReport generateReport(String org) {
        log.info("Generating report for org: {}", org);

        // STEP 1: Get all repos (sequential - we need full list first)
        List<GitHubRepo> repos = gitHubApiClient.getOrgRepositories(org);
        log.info("Found {} repos", repos.size());

        // STEP 2: Shared map - thread safe
        // Key = username, Value = list of repos they can access
        Map<String, List<RepoAccess>> userAccessMap = new ConcurrentHashMap<>();

        // STEP 3: For each repo, launch parallel task
        List<CompletableFuture<Void>> futures = repos.stream()
                .map(repo -> CompletableFuture.runAsync(
                        () -> fetchAndAggregate(org, repo, userAccessMap),
                        gitHubExecutor
                ))
                .collect(Collectors.toList());

        // STEP 4: Wait for ALL parallel tasks to finish
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Report done. Users: {}", userAccessMap.size());

        // STEP 5: Build and return final report
        return AccessReport.builder()
                .organization(org)
                .totalRepositories(repos.size())
                .totalUsers(userAccessMap.size())
                .generatedAt(Instant.now())
                .userAccessMap(userAccessMap)
                .build();
    }

    // This method runs on a background thread for ONE repo
    private void fetchAndAggregate(String org, GitHubRepo repo,
                                   Map<String, List<RepoAccess>> userAccessMap) {
        try {
            // Fetch all collaborators for this one repo
            List<GitHubUser> collaborators = gitHubApiClient.getRepoCollaborators(org, repo.getName());

            for (GitHubUser user : collaborators) {
                // Build one RepoAccess entry for this user
                RepoAccess repoAccess = RepoAccess.builder()
                        .repoName(repo.getName())
                        .repoFullName(repo.getFullName())
                        .repoUrl(repo.getHtmlUrl())
                        .privateRepo(repo.isPrivateRepo())
                        .permission(user.getPermissions() != null
                                ? user.getPermissions().highestRole()
                                : "read")
                        .build();

                // Add to map safely - computeIfAbsent is thread safe
                // "if username not in map yet, create empty list first"
                // "then add this repo to their list"
                userAccessMap
                        .computeIfAbsent(user.getLogin(), k -> new ArrayList<>())
                        .add(repoAccess);
            }

        } catch (Exception e) {
            // Don't crash entire report if one repo fails
            log.warn("Skipping repo {}/{}: {}", org, repo.getName(), e.getMessage());
        }
    }
}