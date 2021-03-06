package com.blocklang.develop.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.StopWatch;

import com.blocklang.core.constant.CmPropKey;
import com.blocklang.core.dao.UserDao;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.PropertyService;
import com.blocklang.core.test.AbstractServiceTest;
import com.blocklang.core.test.TestHelper;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.dao.ProjectBuildProfileDao;
import com.blocklang.develop.dao.ProjectDependenceDao;
import com.blocklang.develop.data.ProjectDependenceData;
import com.blocklang.develop.designer.data.Widget;
import com.blocklang.develop.designer.data.WidgetCategory;
import com.blocklang.develop.designer.data.WidgetProperty;
import com.blocklang.develop.designer.data.RepoWidgetList;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.model.ProjectBuildProfile;
import com.blocklang.develop.model.ProjectContext;
import com.blocklang.develop.model.ProjectDependence;
import com.blocklang.develop.model.ProjectResource;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.develop.service.ProjectService;
import com.blocklang.marketplace.constant.ComponentAttrValueType;
import com.blocklang.marketplace.constant.Language;
import com.blocklang.marketplace.constant.RepoCategory;
import com.blocklang.marketplace.dao.ApiComponentAttrDao;
import com.blocklang.marketplace.dao.ApiComponentDao;
import com.blocklang.marketplace.dao.ApiRepoDao;
import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.dao.ComponentRepoDao;
import com.blocklang.marketplace.dao.ComponentRepoVersionDao;
import com.blocklang.marketplace.model.ApiComponent;
import com.blocklang.marketplace.model.ApiComponentAttr;
import com.blocklang.marketplace.model.ApiRepo;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.model.ComponentRepo;
import com.blocklang.marketplace.model.ComponentRepoVersion;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProjectDependenceServiceImplTest extends AbstractServiceTest{

	@MockBean
	private PropertyService propertyService;
	
	@Autowired
	private ProjectDependenceDao projectDependenceDao;
	@Autowired
	private ProjectDependenceService projectDependenceService;
	@Autowired
	private ProjectBuildProfileDao projectBuildProfileDao;
	@Autowired
	private ComponentRepoDao componentRepoDao;
	@Autowired
	private ComponentRepoVersionDao componentRepoVersionDao;
	@Autowired
	private ApiRepoDao apiRepoDao;
	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ApiComponentDao apiComponentDao;
	@Autowired
	private ApiComponentAttrDao apiComponentAttrDao;
	
	@Test
	public void dev_dependence_exists_that_not_exists() {
		assertThat(projectDependenceService.devDependenceExists(1, 1)).isFalse();
	}
	
	@Test
	public void dev_dependence_exists_that_exists() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		assertThat(projectDependenceService.devDependenceExists(projectId, componentRepoId)).isTrue();
	}
	
	@Test
	public void build_dependence_exists_that_not_exists() {
		assertThat(projectDependenceService.buildDependenceExists(1, 1, AppType.WEB, ProjectBuildProfile.DEFAULT_PROFILE_NAME)).isFalse();
	}
	
	@Test
	public void build_dependence_exists_that_exists() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(version);
		// 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		projectBuildProfileDao.save(profile);
		
		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setProfileId(profile.getId());
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		assertThat(projectDependenceService.buildDependenceExists(projectId, componentRepoId, AppType.WEB, ProjectBuildProfile.DEFAULT_PROFILE_NAME)).isTrue();
	}
	
	@Test
	public void save_failed_component_repo_versions_not_found() {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		Integer userId = 3;
		
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(componentRepoId);
		componentRepo.setAppType(AppType.WEB);
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(0);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(0);
		
		ProjectDependence savedDependence = projectDependenceService.save(projectId, componentRepo, userId);
		
		assertThat(savedDependence).isNull();
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(0);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(0);
	}
	
	@DisplayName("save: when saved dependence is dev dependence then not set profile")
	@Test
	public void save_when_saved_dependence_is_dev_then_not_set_profile(@TempDir Path rootFolder) throws IOException {
		Integer projectId = Integer.MAX_VALUE;
		Integer componentRepoId = Integer.MAX_VALUE - 1;
		Integer userId = 3;
		
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(componentRepoId);
		componentRepo.setAppType(AppType.WEB);
		componentRepo.setIsIdeExtension(true);
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		ProjectDependence savedDependence = projectDependenceService.save(projectId, componentRepo, userId);
		
		assertThat(savedDependence.getProfileId()).isNull();
	}
	
	@Test
	public void save_success(@TempDir Path rootFolder) throws IOException {
		Integer projectId = 1;
		Integer componentRepoId = 2;
		Integer userId = 3;
		
		ComponentRepo componentRepo = new ComponentRepo();
		componentRepo.setId(componentRepoId);
		componentRepo.setAppType(AppType.WEB);
		
		// 创建组件仓库的版本信息
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		version = new ComponentRepoVersion();
		version.setComponentRepoId(componentRepoId);
		version.setVersion("0.2.0");
		version.setGitTagName("v0.2.0");
		version.setApiRepoVersionId(3);
		version.setCreateUserId(11);
		version.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion2 = componentRepoVersionDao.save(version);
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(0);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(0);
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		
		ProjectDependence savedDependence = projectDependenceService.save(projectId, componentRepo, userId);
		
		assertThat(savedDependence.getProjectId()).isEqualTo(projectId);
		// 获取最新版本号
		assertThat(savedDependence.getComponentRepoVersionId()).isEqualTo(savedComponentRepoVersion2.getId());
		assertThat(savedDependence.getProfileId()).isNotNull();
		
		assertThat(countRowsInTable("PROJECT_BUILD_PROFILE")).isEqualTo(1);
		assertThat(countRowsInTable("PROJECT_DEPENDENCE")).isEqualTo(1);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void save_then_update_dependence_json_file(@TempDir Path rootFolder) throws IOException {
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Project project = new Project();
		project.setName("project_name");
		project.setIsPublic(true);
		project.setDescription("description");
		project.setLastActiveTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		ProjectContext context = new ProjectContext("user_name", "project_name", rootFolder.toString());
		
		Project savedProject = projectService.create(userInfo, project);
		
		// 依赖一个 dev 仓库
		ComponentRepo devRepo = new ComponentRepo();
		devRepo.setApiRepoId(1);
		devRepo.setGitRepoUrl("url1");
		devRepo.setGitRepoWebsite("website1");
		devRepo.setGitRepoOwner("jack1");
		devRepo.setGitRepoName("repo1");
		devRepo.setName("name1");
		devRepo.setVersion("version1");
		devRepo.setCategory(RepoCategory.WIDGET);
		devRepo.setCreateUserId(1);
		devRepo.setCreateTime(LocalDateTime.now());
		devRepo.setLanguage(Language.TYPESCRIPT);
		devRepo.setAppType(AppType.WEB);
		devRepo.setIsIdeExtension(true);
		ComponentRepo savedDevRepo = componentRepoDao.save(devRepo);

		ComponentRepoVersion devRepoVersion = new ComponentRepoVersion();
		devRepoVersion.setComponentRepoId(savedDevRepo.getId());
		devRepoVersion.setVersion("0.1.0");
		devRepoVersion.setGitTagName("v0.1.1");
		devRepoVersion.setApiRepoVersionId(3);
		devRepoVersion.setCreateUserId(11);
		devRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(devRepoVersion);
		
		projectDependenceService.save(savedProject.getId(), devRepo, userId);
		
		// 依赖一个 build 仓库
		ComponentRepo buildRepo = new ComponentRepo();
		buildRepo.setApiRepoId(1);
		buildRepo.setGitRepoUrl("url");
		buildRepo.setGitRepoWebsite("website");
		buildRepo.setGitRepoOwner("jack");
		buildRepo.setGitRepoName("repo");
		buildRepo.setName("name");
		buildRepo.setVersion("version");
		buildRepo.setCategory(RepoCategory.WIDGET);
		buildRepo.setCreateUserId(1);
		buildRepo.setCreateTime(LocalDateTime.now());
		buildRepo.setLanguage(Language.TYPESCRIPT);
		buildRepo.setAppType(AppType.WEB);
		ComponentRepo savedBuildRepo = componentRepoDao.save(buildRepo);
		
		ComponentRepoVersion buildRepoVersion = new ComponentRepoVersion();
		buildRepoVersion.setComponentRepoId(savedBuildRepo.getId());
		buildRepoVersion.setVersion("0.1.0");
		buildRepoVersion.setGitTagName("v0.1.0");
		buildRepoVersion.setApiRepoVersionId(3);
		buildRepoVersion.setCreateUserId(11);
		buildRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(buildRepoVersion);
		
		projectDependenceService.save(savedProject.getId(), buildRepo, userId);
		
		// 在 git 仓库中获取 DEPENDENCE.json 文件
		String dependenceFileContent = Files.readString(context.getGitRepositoryDirectory().resolve(ProjectResource.DEPENDENCE_NAME));
		ObjectMapper objectMapper = new ObjectMapper();
		Map jsonObject = objectMapper.readValue(dependenceFileContent, Map.class);
		assertThat(jsonObject).isNotEmpty();
		
		Map dev = (Map)((Map)((Map)jsonObject.get("dev")).get("web")).get("website1/jack1/repo1");
		assertThat(dev.get("git")).isEqualTo("url1");
		assertThat(dev.get("tag")).isEqualTo("v0.1.1");
		
		Map build = (Map)((Map)((Map)((Map)jsonObject.get("build")).get("web")).get("default")).get("website/jack/repo");
		assertThat(build.get("git")).isEqualTo("url");
		assertThat(build.get("tag")).isEqualTo("v0.1.0");
		
		TestHelper.clearDir(rootFolder);
	}
	
	@Test
	public void find_project_dependence_no_dependence() {
		assertThat(projectDependenceService.findProjectDependences(1)).isEmpty();
	}
	
	// 因为当前只支持默认的 Profile，所以不用获取 Profile 信息
	@Test
	public void find_project_dependence_success() {
		Integer projectId = 1;
		// 创建对应的 API 仓库信息
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.CLIENT_API);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion version = new ApiRepoVersion();
		version.setApiRepoId(savedApiRepo.getId());
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(version);
		
		// 创建组件仓库信息
		// 1. dev
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(savedApiRepo.getId());
		repo.setGitRepoUrl("dev_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("dev_name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		repo.setIsIdeExtension(true);
		ComponentRepo savedDevRepo = componentRepoDao.save(repo);
		// 2. build
		repo = new ComponentRepo();
		repo.setApiRepoId(savedApiRepo.getId());
		repo.setGitRepoUrl("build_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("build_name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		ComponentRepo savedBuildRepo = componentRepoDao.save(repo);
		// 创建组件仓库版本信息
		// 1. dev
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedDevRepo.getId());
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion devRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 2. build
		componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedBuildRepo.getId());
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion buildRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 创建一个 dev 依赖（不需创建 Profile）
		ProjectDependence devDependence = new ProjectDependence();
		devDependence.setProjectId(projectId);
		devDependence.setComponentRepoVersionId(devRepoVersion.getId());
		devDependence.setCreateUserId(11);
		devDependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(devDependence);
		
		// 创建一个 build 依赖（需创建默认的 Profile）
		// 1. 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		projectBuildProfileDao.save(profile);
		// 2. 为项目添加一个依赖
		ProjectDependence buildDependence = new ProjectDependence();
		buildDependence.setProjectId(projectId);
		buildDependence.setComponentRepoVersionId(buildRepoVersion.getId());
		buildDependence.setProfileId(profile.getId());
		buildDependence.setCreateUserId(11);
		buildDependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(buildDependence);

		List<ProjectDependenceData> dependences = projectDependenceService.findProjectDependences(projectId);
		
		assertThat(dependences).hasSize(2);
		assertThat(dependences).allMatch(dependence -> dependence != null && 
				dependence.getComponentRepo() != null &&
				dependence.getComponentRepoVersion() != null &&
				dependence.getApiRepo() != null &&
				dependence.getApiRepoVersion() != null
		);
	}
	
	@Test
	public void find_project_dependence_include_std_dependences() {
		Integer userId = 1;
		// 创建一个标准库
		ApiRepo stdApiRepo = new ApiRepo();
		stdApiRepo.setCategory(RepoCategory.WIDGET);
		stdApiRepo.setGitRepoUrl("url");
		stdApiRepo.setGitRepoWebsite("website");
		stdApiRepo.setGitRepoOwner("owner");
		stdApiRepo.setGitRepoName("repo_name");
		stdApiRepo.setName("std-api-widget"); // 默认的标准库
		stdApiRepo.setVersion("0.0.1");
		stdApiRepo.setCreateUserId(userId);
		stdApiRepo.setCreateTime(LocalDateTime.now());
		Integer stdApiRepoId = apiRepoDao.save(stdApiRepo).getId();
		// 为标准库设置一个版本号
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(stdApiRepoId);
		apiVersion.setVersion("0.0.1");
		apiVersion.setGitTagName("v0.0.1");
		apiVersion.setCreateUserId(userId);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在标准库中创建一个 Page 部件
		ApiComponent widget = new ApiComponent();
		String widgetCode = "0001";
		String widgetName = "Page";
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode(widgetCode);
		widget.setName(widgetName);
		widget.setCanHasChildren(true);
		widget.setCreateUserId(userId);
		widget.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget = apiComponentDao.save(widget);
		// 为 Page 部件添加一个属性
		ApiComponentAttr widgetProperty = new ApiComponentAttr();
		widgetProperty.setApiComponentId(savedWidget.getId());
		widgetProperty.setCode("0011");
		widgetProperty.setName("prop_name");
		widgetProperty.setDefaultValue("default_value");
		widgetProperty.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty);
		
		// 创建一个 ide 版的组件库
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(stdApiRepoId);
		repo.setGitRepoUrl("url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("std-ide-widget");
		repo.setLabel("label");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		repo.setStd(true);
		repo.setIsIdeExtension(true);
		ComponentRepo savedComponentRepo = componentRepoDao.save(repo);
		// 创建一个 ide 版的组件库版本
		ComponentRepoVersion version = new ComponentRepoVersion();
		version.setComponentRepoId(savedComponentRepo.getId());
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setApiRepoVersionId(savedApiRepoVersion.getId());
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(version);
		
		when(propertyService.findStringValue(CmPropKey.STD_WIDGET_IDE_NAME, "std-ide-widget")).thenReturn("std-ide-widget");
		when(propertyService.findIntegerValue(CmPropKey.STD_WIDGET_REGISTER_USERID, 1)).thenReturn(1);
		// 注意，所有项目都会默认包含标准库
		List<ProjectDependenceData> dependences = projectDependenceService.findProjectDependences(Integer.MAX_VALUE, true);
		
		assertThat(dependences).hasSize(1);
	}
	
	@DisplayName("find project's build dependences: has no dependences")
	@Test
	public void findProjectBuildDependences_no_dependence() {
		int projectId = Integer.MAX_VALUE;
		assertThat(projectDependenceService.findProjectBuildDependences(projectId)).isEmpty();
	}
	
	@DisplayName("find project's build dependences: should not contains dev dependences")
	@Test
	public void findProjectBuildDependences_not_contains_dev_dependence() {
		Integer projectId = Integer.MAX_VALUE;
		// 创建对应的 API 仓库信息
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.CLIENT_API);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion version = new ApiRepoVersion();
		version.setApiRepoId(savedApiRepo.getId());
		version.setVersion("0.1.0");
		version.setGitTagName("v0.1.0");
		version.setCreateUserId(1);
		version.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(version);
		
		// 创建组件仓库信息
		// 1. dev
		ComponentRepo repo = new ComponentRepo();
		repo.setApiRepoId(savedApiRepo.getId());
		repo.setGitRepoUrl("dev_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("dev_name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		repo.setIsIdeExtension(true);
		ComponentRepo savedDevRepo = componentRepoDao.save(repo);
		// 2. build
		repo = new ComponentRepo();
		repo.setApiRepoId(savedApiRepo.getId());
		repo.setGitRepoUrl("build_url");
		repo.setGitRepoWebsite("website");
		repo.setGitRepoOwner("jack");
		repo.setGitRepoName("repo");
		repo.setName("build_name");
		repo.setVersion("version");
		repo.setCategory(RepoCategory.WIDGET);
		repo.setCreateUserId(1);
		repo.setCreateTime(LocalDateTime.now());
		repo.setLanguage(Language.TYPESCRIPT);
		repo.setAppType(AppType.WEB);
		ComponentRepo savedBuildRepo = componentRepoDao.save(repo);
		// 创建组件仓库版本信息
		// 1. dev
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedDevRepo.getId());
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion devRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 2. build
		componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(savedBuildRepo.getId());
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion buildRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		// 创建一个 dev 依赖（不需创建 Profile）
		ProjectDependence devDependence = new ProjectDependence();
		devDependence.setProjectId(projectId);
		devDependence.setComponentRepoVersionId(devRepoVersion.getId());
		devDependence.setCreateUserId(11);
		devDependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(devDependence);
		
		// 创建一个 build 依赖（需创建默认的 Profile）
		// 1. 为项目的 web 页面添加一个默认的 Profile
		ProjectBuildProfile profile = new ProjectBuildProfile();
		profile.setProjectId(projectId);
		profile.setAppType(AppType.WEB);
		profile.setName(ProjectBuildProfile.DEFAULT_PROFILE_NAME);
		profile.setCreateUserId(11);
		profile.setCreateTime(LocalDateTime.now());
		projectBuildProfileDao.save(profile);
		// 2. 为项目添加一个依赖
		ProjectDependence buildDependence = new ProjectDependence();
		buildDependence.setProjectId(projectId);
		buildDependence.setComponentRepoVersionId(buildRepoVersion.getId());
		buildDependence.setProfileId(profile.getId());
		buildDependence.setCreateUserId(11);
		buildDependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(buildDependence);

		List<ProjectDependenceData> dependences = projectDependenceService.findProjectBuildDependences(projectId);
		
		assertThat(dependences).hasSize(1);
		assertThat(dependences.get(0).getComponentRepoVersion().getId()).isEqualTo(buildRepoVersion.getId());
	}
	
	
	// 注意，因为这些都是在一个事务中完成的，所以不要直接通过获取数据库表的记录数来断言，
	// 而是通过 jpa 的 findById 来断言，因为事务没有结束，数据库可能还没有执行真正做删除操作。
	@Test
	public void delete_success() {
		// 为项目添加一个依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(1);
		dependence.setComponentRepoVersionId(2);
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		assertThat(projectDependenceDao.findById(savedDependence.getId())).isPresent();
		projectDependenceService.delete(savedDependence.getId());
		assertThat(projectDependenceDao.findById(savedDependence.getId())).isEmpty();
	}

	@Test
	public void delete_then_update_dependence_json_file(@TempDir Path rootFolder) throws IOException {
		// 一、创建一个项目
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Project project = new Project();
		project.setName("project_name");
		project.setIsPublic(true);
		project.setDescription("description");
		project.setLastActiveTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		ProjectContext context = new ProjectContext("user_name", "project_name", rootFolder.toString());
		
		Project savedProject = projectService.create(userInfo, project);
		
		// 二、添加一个依赖
		// 依赖一个 dev 仓库
		ComponentRepo devRepo = new ComponentRepo();
		devRepo.setApiRepoId(1);
		devRepo.setGitRepoUrl("url1");
		devRepo.setGitRepoWebsite("website1");
		devRepo.setGitRepoOwner("jack1");
		devRepo.setGitRepoName("repo1");
		devRepo.setName("name1");
		devRepo.setVersion("version1");
		devRepo.setCategory(RepoCategory.WIDGET);
		devRepo.setCreateUserId(1);
		devRepo.setCreateTime(LocalDateTime.now());
		devRepo.setLanguage(Language.TYPESCRIPT);
		devRepo.setAppType(AppType.WEB);
		devRepo.setIsIdeExtension(true);
		ComponentRepo savedDevRepo = componentRepoDao.save(devRepo);

		ComponentRepoVersion devRepoVersion = new ComponentRepoVersion();
		devRepoVersion.setComponentRepoId(savedDevRepo.getId());
		devRepoVersion.setVersion("0.1.0");
		devRepoVersion.setGitTagName("v0.1.1");
		devRepoVersion.setApiRepoVersionId(3);
		devRepoVersion.setCreateUserId(11);
		devRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(devRepoVersion);
		
		ProjectDependence savedDependence = projectDependenceService.save(savedProject.getId(), devRepo, userId);
		
		// 三、删除添加的依赖
		projectDependenceService.delete(savedDependence.getId());
		
		// 四、断言 DEPENDENCE.json 文件的内容为 “{ }”
		// 不直接转换为 java 对象判断，而是判断文本，这样可以断言美化后的 json
		// 在 git 仓库中获取 DEPENDENCE.json 文件
		String dependenceFileContent = Files.readString(context.getGitRepositoryDirectory().resolve(ProjectResource.DEPENDENCE_NAME));
		assertThat(dependenceFileContent).isEqualTo("{ }");
		
		TestHelper.clearDir(rootFolder);
	}
	
	@Test
	public void update_success() {
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(1);
		dependence.setComponentRepoVersionId(2);
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		
		savedDependence.setComponentRepoVersionId(3);
		
		assertThat(projectDependenceService.update(savedDependence).getComponentRepoVersionId()).isEqualTo(3);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void update_then_update_dependence_json_file(@TempDir Path rootFolder) throws IOException {
		// 一、创建一个项目
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginName("user_name");
		userInfo.setAvatarUrl("avatar_url");
		userInfo.setEmail("email");
		userInfo.setMobile("mobile");
		userInfo.setCreateTime(LocalDateTime.now());
		Integer userId = userDao.save(userInfo).getId();
		
		Project project = new Project();
		project.setName("project_name");
		project.setIsPublic(true);
		project.setDescription("description");
		project.setLastActiveTime(LocalDateTime.now());
		project.setCreateUserId(userId);
		project.setCreateTime(LocalDateTime.now());
		project.setCreateUserName("user_name");
		
		when(propertyService.findStringValue(CmPropKey.BLOCKLANG_ROOT_PATH)).thenReturn(Optional.of(rootFolder.toString()));
		ProjectContext context = new ProjectContext("user_name", "project_name", rootFolder.toString());
		
		Project savedProject = projectService.create(userInfo, project);
		
		// 二、添加一个依赖
		// 依赖一个 dev 仓库
		ComponentRepo devRepo = new ComponentRepo();
		devRepo.setApiRepoId(1);
		devRepo.setGitRepoUrl("url1");
		devRepo.setGitRepoWebsite("website1");
		devRepo.setGitRepoOwner("jack1");
		devRepo.setGitRepoName("repo1");
		devRepo.setName("name1");
		devRepo.setVersion("version1");
		devRepo.setCategory(RepoCategory.WIDGET);
		devRepo.setCreateUserId(1);
		devRepo.setCreateTime(LocalDateTime.now());
		devRepo.setLanguage(Language.TYPESCRIPT);
		devRepo.setAppType(AppType.WEB);
		devRepo.setIsIdeExtension(true);
		ComponentRepo savedDevRepo = componentRepoDao.save(devRepo);

		ComponentRepoVersion devRepoVersion = new ComponentRepoVersion();
		devRepoVersion.setComponentRepoId(savedDevRepo.getId());
		devRepoVersion.setVersion("0.1.0");
		devRepoVersion.setGitTagName("v0.1.1");
		devRepoVersion.setApiRepoVersionId(3);
		devRepoVersion.setCreateUserId(11);
		devRepoVersion.setCreateTime(LocalDateTime.now());
		componentRepoVersionDao.save(devRepoVersion);
		
		ProjectDependence savedDependence = projectDependenceService.save(savedProject.getId(), devRepo, userId);
		
		// 三、修改添加的依赖
		ComponentRepoVersion newDevRepoVersion = new ComponentRepoVersion();
		newDevRepoVersion.setComponentRepoId(savedDevRepo.getId());
		newDevRepoVersion.setVersion("0.2.0");
		newDevRepoVersion.setGitTagName("v0.2.0");
		newDevRepoVersion.setApiRepoVersionId(3);
		newDevRepoVersion.setCreateUserId(11);
		newDevRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedNewDevRepoVersion = componentRepoVersionDao.save(newDevRepoVersion);
		
		savedDependence.setComponentRepoVersionId(savedNewDevRepoVersion.getId());
		
		projectDependenceService.update(savedDependence);
		
		// 四、断言 DEPENDENCE.json 文件的内容
		// 在 git 仓库中获取 DEPENDENCE.json 文件
		String dependenceFileContent = Files.readString(context.getGitRepositoryDirectory().resolve(ProjectResource.DEPENDENCE_NAME));
		ObjectMapper objectMapper = new ObjectMapper();
		Map jsonObject = objectMapper.readValue(dependenceFileContent, Map.class);
		assertThat(jsonObject).isNotEmpty();
		
		Map dev = (Map)((Map)((Map)jsonObject.get("dev")).get("web")).get("website1/jack1/repo1");
		assertThat(dev.get("git")).isEqualTo("url1");
		assertThat(dev.get("tag")).isEqualTo("v0.2.0");
		
		TestHelper.clearDir(rootFolder);
	}
	
	@Test
	public void find_by_id_no_data() {
		assertThat(projectDependenceService.findById(1)).isEmpty();
	}
	
	@Test
	public void find_by_id_success() {
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(1);
		dependence.setComponentRepoVersionId(2);
		dependence.setCreateUserId(11);
		dependence.setCreateTime(LocalDateTime.now());
		ProjectDependence savedDependence = projectDependenceDao.save(dependence);
		
		assertThat(projectDependenceService.findById(savedDependence.getId())).isPresent();
	}
	
	@Test
	public void find_all_widgets_no_data() {
		List<RepoWidgetList> result = projectDependenceService.findAllWidgets(1);
		assertThat(result).isEmpty();
	}
	
	@Test
	public void find_all_widgets_uncategory() {
		Integer projectId = 1;
		
		// 创建一个 API 仓库，类型是  Widget
		String apiRepoName = "api_repo_name";
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName(apiRepoName);
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在版本下创建一个部件，没有分类
		ApiComponent widget = new ApiComponent();
		String widgetCode = "0001";
		String widgetName = "Widget1";
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode(widgetCode);
		widget.setName(widgetName);
		widget.setLabel("Wiget 1");
		widget.setDescription("Description");
		widget.setCanHasChildren(false);
		widget.setCreateUserId(1);
		widget.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget = apiComponentDao.save(widget);
		
		// 为部件设置一个属性
		ApiComponentAttr widgetProperty = new ApiComponentAttr();
		widgetProperty.setApiComponentId(savedWidget.getId());
		widgetProperty.setCode("0011");
		widgetProperty.setName("prop_name");
		widgetProperty.setValueType(ComponentAttrValueType.STRING);
		apiComponentAttrDao.save(widgetProperty);
		
		// 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1);
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 将组件仓库添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		StopWatch watch = new StopWatch();
		watch.start();
		List<RepoWidgetList> result = projectDependenceService.findAllWidgets(projectId);
		watch.stop();
		System.out.println("毫秒：" + watch.getTotalTimeMillis());
		
		assertThat(result).hasSize(1);
		// 测试未分组的情况
		RepoWidgetList repo = result.get(0);
		assertThat(repo.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(repo.getApiRepoName()).isEqualTo(apiRepoName);
		assertThat(repo.getWidgetCategories()).hasSize(1);
		
		WidgetCategory category1 = repo.getWidgetCategories().get(0);
		assertThat(category1.getName()).isEqualTo("_");
		assertThat(category1.getWidgets()).hasSize(1);
		
		Widget widget1 = category1.getWidgets().get(0);
		// Widget 中的 apiRepoId 是一个冗余字段
		assertThat(widget1.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(widget1.getWidgetId()).isEqualTo(savedWidget.getId());
		assertThat(widget1.getWidgetCode()).isEqualTo(widgetCode);
		assertThat(widget1.getWidgetName()).isEqualTo(widgetName);
		assertThat(widget1.getCanHasChildren()).isFalse();
		
		WidgetProperty property = widget1.getProperties().get(0);
		assertThat(property.getCode()).isEqualTo("0011");
		assertThat(property.getName()).isEqualTo("prop_name");
		assertThat(property.getValueType()).isEqualTo("string");
		assertThat(property.getDefaultValue()).isNull();
	}
	
	// 项目依赖两个组件库，但两个组件库实现同一个 api
	@Test
	public void find_all_widgets_two_component_repo_impl_one_api_repo() {
		Integer projectId = 1;
		
		// 创建一个 API 仓库，类型是 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在版本下创建一个部件，没有分类
		ApiComponent widget = new ApiComponent();
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode("0001");
		widget.setName("Widget1");
		widget.setLabel("Wiget 1");
		widget.setDescription("Description");
		widget.setCanHasChildren(false);
		widget.setCreateUserId(1);
		widget.setCreateTime(LocalDateTime.now());
		apiComponentDao.save(widget);
		
		// 第一个依赖
		// 1. 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1); // 组件仓库 id 为 1
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 2. 将组件仓库添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		// 第二个依赖
		// 1. 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(2); // 组件仓库 id 为 2
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId()); // 实现的是同一个 api repo version
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 2. 将组件仓库添加为项目依赖
		dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		StopWatch watch = new StopWatch();
		watch.start();
		List<RepoWidgetList> result = projectDependenceService.findAllWidgets(projectId);
		watch.stop();
		System.out.println("毫秒：" + watch.getTotalTimeMillis());
		assertThat(result).hasSize(1);
	}
	
	// 只过滤出 Widget 类型的 API REPO
	@Test
	public void find_all_widgets_only_filter_widget_api_repo() {
		Integer projectId = 1;
		
		// 创建一个 API 仓库，类型是 Widget
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName("e");
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.CLIENT_API); // 不是 Widget 仓库
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1);
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 将组件仓库添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		List<RepoWidgetList> result = projectDependenceService.findAllWidgets(projectId);
		assertThat(result).hasSize(0);
	}

	// 测试一个 API 仓库中有两个分组的情况
	// 尚不支持 category 字段
	@Disabled
	@Test
	public void find_all_widgets_two_category() {
		Integer projectId = 1;
		
		// 创建一个 API 仓库，类型是  Widget
		String apiRepoName = "api_repo_name";
		ApiRepo apiRepo = new ApiRepo();
		apiRepo.setGitRepoUrl("a");
		apiRepo.setGitRepoWebsite("b");
		apiRepo.setGitRepoOwner("c");
		apiRepo.setGitRepoName("d");
		apiRepo.setName(apiRepoName);
		apiRepo.setVersion("f");
		apiRepo.setCategory(RepoCategory.WIDGET);
		apiRepo.setCreateUserId(1);
		apiRepo.setCreateTime(LocalDateTime.now());
		ApiRepo savedApiRepo = apiRepoDao.save(apiRepo);
		
		// 创建对应的 API 仓库版本信息
		ApiRepoVersion apiVersion = new ApiRepoVersion();
		apiVersion.setApiRepoId(savedApiRepo.getId());
		apiVersion.setVersion("0.1.0");
		apiVersion.setGitTagName("v0.1.0");
		apiVersion.setCreateUserId(1);
		apiVersion.setCreateTime(LocalDateTime.now());
		ApiRepoVersion savedApiRepoVersion = apiRepoVersionDao.save(apiVersion);

		// 在版本下创建一个部件，没有分类
		ApiComponent widget = new ApiComponent();
		String widgetCode = "0001";
		String widgetName = "Widget1";
		widget.setApiRepoVersionId(savedApiRepoVersion.getId());
		widget.setCode(widgetCode);
		widget.setName(widgetName);
		widget.setLabel("Wiget 1");
		widget.setDescription("Description");
		widget.setCanHasChildren(false);
		widget.setCreateUserId(1);
		widget.setCreateTime(LocalDateTime.now());
		ApiComponent savedWidget = apiComponentDao.save(widget);
		
		// TODO: 添加两个部件，分别属于不同种类。
		
		// 在组件仓库版本信息中创建一条记录，引用 api 版本信息
		ComponentRepoVersion componentRepoVersion = new ComponentRepoVersion();
		componentRepoVersion.setComponentRepoId(1);
		componentRepoVersion.setVersion("0.1.0");
		componentRepoVersion.setGitTagName("v0.1.0");
		componentRepoVersion.setApiRepoVersionId(savedApiRepoVersion.getId());
		componentRepoVersion.setCreateUserId(1);
		componentRepoVersion.setCreateTime(LocalDateTime.now());
		ComponentRepoVersion savedComponentRepoVersion = componentRepoVersionDao.save(componentRepoVersion);
		
		// 将组件仓库添加为项目依赖
		ProjectDependence dependence = new ProjectDependence();
		dependence.setProjectId(projectId);
		dependence.setComponentRepoVersionId(savedComponentRepoVersion.getId());
		dependence.setCreateUserId(1);
		dependence.setCreateTime(LocalDateTime.now());
		projectDependenceDao.save(dependence);
		
		StopWatch watch = new StopWatch();
		watch.start();
		List<RepoWidgetList> result = projectDependenceService.findAllWidgets(projectId);
		watch.stop();
		System.out.println("毫秒：" + watch.getTotalTimeMillis());
		
		assertThat(result).hasSize(1);
		// 测试未分组的情况
		RepoWidgetList repo = result.get(0);
		assertThat(repo.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(repo.getApiRepoName()).isEqualTo(apiRepoName);
		assertThat(repo.getWidgetCategories()).hasSize(1);
		
		WidgetCategory category1 = repo.getWidgetCategories().get(0);
		assertThat(category1.getName()).isEqualTo("_");
		assertThat(category1.getWidgets()).hasSize(1);
		
		Widget widget1 = category1.getWidgets().get(0);
		// Widget 中的 apiRepoId 是一个冗余字段
		assertThat(widget1.getApiRepoId()).isEqualTo(savedApiRepo.getId());
		assertThat(widget1.getWidgetId()).isEqualTo(savedWidget.getId());
		assertThat(widget1.getWidgetCode()).isEqualTo(widgetCode);
		assertThat(widget1.getWidgetName()).isEqualTo(widgetName);
		assertThat(widget1.getCanHasChildren()).isFalse();
	}
}
