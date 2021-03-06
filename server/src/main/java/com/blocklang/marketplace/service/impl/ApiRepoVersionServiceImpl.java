package com.blocklang.marketplace.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blocklang.marketplace.dao.ApiRepoVersionDao;
import com.blocklang.marketplace.model.ApiRepoVersion;
import com.blocklang.marketplace.service.ApiRepoVersionService;

import de.skuzzle.semantic.Version;

@Service
public class ApiRepoVersionServiceImpl implements ApiRepoVersionService {

	@Autowired
	private ApiRepoVersionDao apiRepoVersionDao;
	
	@Override
	public Optional<ApiRepoVersion> findById(Integer apiRepoVersionId) {
		return apiRepoVersionDao.findById(apiRepoVersionId);
	}

	@Override
	public Optional<ApiRepoVersion> findLatestVersion(Integer apiRepoId) {
		List<ApiRepoVersion> allVersions = apiRepoVersionDao.findAllByApiRepoId(apiRepoId);
		if(allVersions.isEmpty()) {
			return Optional.empty();
		}
		// 最新版本在最前面
		allVersions.sort(new Comparator<ApiRepoVersion>() {
			@Override
			public int compare(ApiRepoVersion version1, ApiRepoVersion version2) {
				return Version.compare(Version.parseVersion(version2.getVersion()), Version.parseVersion(version1.getVersion()));
			}
		});
		return Optional.of(allVersions.get(0));
	}

}
