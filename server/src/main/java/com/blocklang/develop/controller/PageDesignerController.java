package com.blocklang.develop.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.develop.designer.data.Dependence;
import com.blocklang.develop.model.Project;
import com.blocklang.develop.service.ProjectDependenceService;
import com.blocklang.marketplace.model.ComponentRepo;

/**
 * 页面设计器专用的控制器放在此处集中维护。
 * 
 * @author jinzw
 *
 */
@RestController
public class PageDesignerController extends AbstractProjectController {
	
	@Autowired
	private ProjectDependenceService projectDependenceService;

	/**
	 * 与 {@link ProjectDependenceController#getDependence(Principal, String, String)}} 功能类似，
	 * 但是一个是在项目依赖的维护页面中使用的，一个是在页面设计器中使用的。
	 * 
	 * @param principal
	 * @param owner
	 * @param projectName
	 * @return
	 */
	@GetMapping("/designer/projects/{projectId}/dependences")
	public ResponseEntity<List<Dependence>> listProjectDependences(
			Principal principal,
			@PathVariable Integer projectId,
			@RequestParam String category) {
		if(!category.equalsIgnoreCase("dev")) {
			throw new NotImplementedException("当前仅支持获取 dev 依赖。");
		}
		
		Project project = projectService.findById(projectId).orElseThrow(ResourceNotFoundException::new);
		ensureCanRead(principal, project);
		
		List<Dependence> result = projectDependenceService.findProjectDependences(project.getId()).stream().filter(item -> item.getComponentRepo().getIsIdeExtension()).map(item -> {
			Dependence dependence = new Dependence();
			
			ComponentRepo componentRepo = item.getComponentRepo();
			dependence.setId(componentRepo.getId());
			dependence.setGitRepoWebsite(componentRepo.getGitRepoWebsite());
			dependence.setGitRepoOwner(componentRepo.getGitRepoOwner());
			dependence.setGitRepoName(componentRepo.getGitRepoName());
			dependence.setName(componentRepo.getName());
			dependence.setCategory(componentRepo.getCategory().getValue());
			dependence.setStd(componentRepo.isStd());
			
			dependence.setVersion(item.getComponentRepoVersion().getVersion());
			
			dependence.setApiRepoId(item.getApiRepo().getId());
			return dependence;
		}).collect(Collectors.toList());
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 获取项目依赖的 API 组件库中类型为 Widget 的组件库中的所有部件。
	 * 并按照组件库和部件种类分组。
	 * 
	 * @return
	 */
//	@GetMapping("/projects/{owner}/{projectName}/dependences/widgets")
//	public ResponseEntity<List<WidgetRepo>> getAllDependenceWidgets(
//			Principal principal,
//			@PathVariable String owner,
//			@PathVariable String projectName) {
//		Project project = projectService.find(owner, projectName).orElseThrow(ResourceNotFoundException::new);
//
//		ensureCanRead(principal, project);
//		
//		List<WidgetRepo> result = projectDependenceService.findAllWidgets(project.getId());
//		return ResponseEntity.ok(result);
//	}
}