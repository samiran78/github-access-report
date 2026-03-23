package com.samiran.github_access_report.client;

import com.samiran.github_access_report.exception.GitHubApiException;
import com.samiran.github_access_report.model.GitHubRepo;
import com.samiran.github_access_report.model.GitHubUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubApiClient {
    private final RestTemplate GithubRestTemplate;
    @org.springframework.beans.factory.annotation.Value("${github.api.per-page}")
    private int apiPerpage;
   @Value("${github.api.base-url}")
    private String baseUrl;
    // Method 1: Get all repos of an org
   public List<GitHubRepo> getOrgRepositories(String organization){
String url = baseUrl + "/orgs/{org}/repos?per_page=" + apiPerpage + "&page={page}&type=all";
//logging
       log.info("Fetching repos for org: {}",organization);
       return fetchAllPage(url,organization,new ParameterizedTypeReference<List<GitHubRepo>>(){});
   }
   //method to get all githubcollab-users
    public List<GitHubUser> getRepoCollaborators(String organization,String repoName){
        String url = baseUrl + "/repos/{org}/" + repoName
                + "/collaborators?per_page=" + apiPerpage + "&page={page}&affiliation=all";
        log.debug("Fetching collaborators for: {}/{}", organization, repoName);
        return fetchAllPage(url, organization, new ParameterizedTypeReference<List<GitHubUser>>() {});
    }

    private <T> List<T> fetchAllPage(String url, String organization, ParameterizedTypeReference<List<T>> typeReference) {
//        create empty list called allResults
        List<T> allResults = new ArrayList<>();
        //set page bydefault
        int pageNo =  1;
        while(true){
            try {
                //we will perform he operation here
//                exchange() call with current page number
                ResponseEntity<List<T>> responseAll  = GithubRestTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        typeReference,
                        organization,
                        pageNo
                );
//                get the body from response → call it pageData
                List<T> pageData = responseAll.getBody();
                if(pageData==null || pageData.isEmpty()){
                    break;
                }
                //   add pageData to allResults
                allResults.addAll(pageData);
                if(pageData.size()<apiPerpage){
                    break;
                }
                //move/incremnet page
                pageNo++;

            } catch (HttpClientErrorException.NotFound e) {
                throw new GitHubApiException("Organization not found: " + organization);
            }catch (HttpClientErrorException.Unauthorized e){
                throw new GitHubApiException("Token missing or invalid. Set GITHUB_TOKEN.");
            }catch (HttpClientErrorException.Forbidden e){
                throw new GitHubApiException("Token lacks permissions. Need: repo, read:org");
            }catch (HttpClientErrorException E){
                if(E.getStatusCode().value()==429){
                    throw new GitHubApiException("GitHub rate limit hit. Wait and retry.");
                }
                throw new GitHubApiException("GitHub API error: " + E.getMessage());
            }
        }
        return allResults;
    }
}
