package com.blocklang.marketplace.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.blocklang.core.model.PartialOperateFields;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.constant.converter.RepoCategoryConverter;

@Entity
@Table(name = "api_repo", 
uniqueConstraints = {
	@UniqueConstraint(columnNames = { "create_user_id", "git_repo_url" }),
	@UniqueConstraint(columnNames = { "create_user_id", "name" }) 
}
)
public class ApiRepo extends PartialOperateFields{

	private static final long serialVersionUID = -7380348480679301101L;

	@Column(name = "git_repo_url", nullable = false, length = 128)
	private String gitRepoUrl;

	@Column(name = "git_repo_website", nullable = false, length = 32)
	private String gitRepoWebsite;

	@Column(name = "git_repo_owner", nullable = false, length = 64)
	private String gitRepoOwner;

	@Column(name = "git_repo_name", nullable = false, length = 64)
	private String gitRepoName;

	@Column(name = "name", nullable = false, length = 64)
	private String name;

	@Column(name = "version", nullable = false, length = 32)
	private String version;

	@Column(name = "label", length = 32)
	private String label;

	@Column(name = "description", length = 512)
	private String description;

	@Convert(converter = RepoCategoryConverter.class)
	@Column(name = "category", nullable = false, length = 2)
	private RepoCategory category;
	
	@Column(name = "last_publish_time" )
	private LocalDateTime lastPublishTime;

	public String getGitRepoUrl() {
		return gitRepoUrl;
	}

	public void setGitRepoUrl(String gitRepoUrl) {
		this.gitRepoUrl = gitRepoUrl;
	}

	public String getGitRepoWebsite() {
		return gitRepoWebsite;
	}

	public void setGitRepoWebsite(String gitRepoWebsite) {
		this.gitRepoWebsite = gitRepoWebsite;
	}

	public String getGitRepoOwner() {
		return gitRepoOwner;
	}

	public void setGitRepoOwner(String gitRepoOwner) {
		this.gitRepoOwner = gitRepoOwner;
	}

	public String getGitRepoName() {
		return gitRepoName;
	}

	public void setGitRepoName(String gitRepoName) {
		this.gitRepoName = gitRepoName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RepoCategory getCategory() {
		return category;
	}

	public void setCategory(RepoCategory category) {
		this.category = category;
	}

	public LocalDateTime getLastPublishTime() {
		return lastPublishTime;
	}

	public void setLastPublishTime(LocalDateTime lastPublishTime) {
		this.lastPublishTime = lastPublishTime;
	}
	
}
