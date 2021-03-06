import WidgetBase from '@dojo/framework/core/WidgetBase';
import { v, w } from '@dojo/framework/core/vdom';
import Outlet from '@dojo/framework/routing/Outlet';

import About from './widgets/About';

import 'bootstrap';

import * as css from './App.m.css';
import Exception from './pages/error/Exception';

import HeaderContainer from './containers/HeaderContainer';
import HomeContainer from './containers/HomeContainer';
import NewProjectContainer from './containers/project/NewProjectContainer';
import ViewProjectContainer from './containers/project/ViewProjectContainer';
import ListReleaseContainer from './containers/release/ListReleaseContainer';
import NewReleaseContainer from './containers/release/NewReleaseContainer';
import ViewDocumentContainer from './containers/help/ViewDocumentContainer';
import ProfileContainer from './containers/user/settings/ProfileContainer';
import CompleteUserInfoContainer from './containers/user/CompleteUserInfoContainer';
import ViewReleaseContainer from './containers/release/ViewReleaseContainer';
import NewPageContainer from './containers/resource/NewPageContainer';
import NewGroupContainer from './containers/resource/NewGroupContainer';
import ViewProjectGroupContainer from './containers/project/ViewProjectGroupContainer';
import ListComponentRepoContainer from './containers/marketplace/ListComponentRepoContainer';
import ListMyComponentRepoContainer from './containers/user/settings/ListMyComponentRepoContainer';
import ViewComponentRepoPublishTaskContainer from './containers/user/settings/ViewComponentRepoPublishTaskContainer';
import ViewProjectReadmeContainer from './containers/project/ViewProjectReadmeContainer';
import ViewProjectDependenceContainer from './containers/project/ViewProjectDependenceContainer';
import ViewProjectPageContainer from './containers/project/ViewProjectPageContainer';
import ViewProjectTempletContainer from './containers/project/ViewProjectTempletContainer';
import ViewProjectServiceContainer from './containers/project/ViewProjectServiceContainer';

import { library } from '@fortawesome/fontawesome-svg-core';

import { faGithub } from '@fortawesome/free-brands-svg-icons/faGithub';
import { faQq } from '@fortawesome/free-brands-svg-icons/faQq';
import { faFirefox } from '@fortawesome/free-brands-svg-icons/faFirefox';
import { faAndroid } from '@fortawesome/free-brands-svg-icons/faAndroid';
import { faApple } from '@fortawesome/free-brands-svg-icons/faApple';
import { faWeixin } from '@fortawesome/free-brands-svg-icons/faWeixin';
import { faAlipay } from '@fortawesome/free-brands-svg-icons/faAlipay';
import { faJava } from '@fortawesome/free-brands-svg-icons/faJava';
import { faGitAlt } from '@fortawesome/free-brands-svg-icons/faGitAlt';

import { faFolder } from '@fortawesome/free-solid-svg-icons/faFolder';
import { faSquare } from '@fortawesome/free-solid-svg-icons/faSquare';
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus';
import { faBookOpen } from '@fortawesome/free-solid-svg-icons/faBookOpen';
import { faEdit } from '@fortawesome/free-solid-svg-icons/faEdit';
import { faInfoCircle } from '@fortawesome/free-solid-svg-icons/faInfoCircle';
import { faLock } from '@fortawesome/free-solid-svg-icons/faLock';
import { faSignOutAlt } from '@fortawesome/free-solid-svg-icons/faSignOutAlt';
import { faHome } from '@fortawesome/free-solid-svg-icons/faHome';
import { faNewspaper } from '@fortawesome/free-solid-svg-icons/faNewspaper';
import { faPlug } from '@fortawesome/free-solid-svg-icons/faPlug';
import { faTag } from '@fortawesome/free-solid-svg-icons/faTag';
import { faClock } from '@fortawesome/free-solid-svg-icons/faClock';
import { faSpinner } from '@fortawesome/free-solid-svg-icons/faSpinner';
import { faTimes } from '@fortawesome/free-solid-svg-icons/faTimes';
import { faCheck } from '@fortawesome/free-solid-svg-icons/faCheck';
import { faBan } from '@fortawesome/free-solid-svg-icons/faBan';
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog';
import { faUser } from '@fortawesome/free-solid-svg-icons/faUser';
import { faCodeBranch } from '@fortawesome/free-solid-svg-icons/faCodeBranch';
import { faCopy } from '@fortawesome/free-solid-svg-icons/faCopy';
import { faMinus } from '@fortawesome/free-solid-svg-icons/faMinus';
import { faSearch } from '@fortawesome/free-solid-svg-icons/faSearch';
import { faLightbulb } from '@fortawesome/free-solid-svg-icons/faLightbulb';
import { faPuzzlePiece } from '@fortawesome/free-solid-svg-icons/faPuzzlePiece';
import { faCube } from '@fortawesome/free-solid-svg-icons/faCube';
import FooterContainer from './containers/FooterContainer';

library.add(
	faGithub,
	faQq,
	faFirefox,
	faAndroid,
	faApple,
	faWeixin,
	faAlipay,
	faJava,
	faGitAlt,

	faPlus,
	faBookOpen,
	faEdit,
	faInfoCircle,
	faLock,
	faSignOutAlt,
	faHome,
	faFolder,
	faSquare,
	faNewspaper,
	faPlug,
	faTag,
	faClock,
	faSpinner,
	faTimes,
	faCheck,
	faBan,
	faCog,
	faUser,
	faCodeBranch,
	faCopy,
	faMinus,
	faSearch,
	faLightbulb,
	faPuzzlePiece,
	faCube
);

export default class App extends WidgetBase {
	protected render() {
		return v('div', { classes: [css.root] }, [
			w(HeaderContainer, {}),
			v('div', { classes: css.content }, [
				w(Outlet, { key: 'home', id: 'home', renderer: () => w(HomeContainer, {}) }),
				w(Outlet, {
					key: 'complete-user-info',
					id: 'complete-user-info',
					renderer: () => w(CompleteUserInfoContainer, {}),
				}),
				w(Outlet, {
					key: 'new-project',
					id: 'new-project',
					renderer: () => w(NewProjectContainer, {}),
				}),
				w(Outlet, { key: 'view-project', id: 'view-project', renderer: () => w(ViewProjectContainer, {}) }),
				w(Outlet, {
					key: 'view-project-group',
					id: 'view-project-group',
					renderer: () => w(ViewProjectGroupContainer, {}),
				}),
				w(Outlet, {
					key: 'view-project-readme',
					id: 'view-project-readme',
					renderer: () => w(ViewProjectReadmeContainer, {}),
				}),
				w(Outlet, {
					key: 'view-project-dependence',
					id: 'view-project-dependence',
					renderer: () => w(ViewProjectDependenceContainer, {}),
				}),
				w(Outlet, {
					key: 'view-project-page',
					id: 'view-project-page',
					renderer: () => w(ViewProjectPageContainer, {}),
				}),
				w(Outlet, {
					key: 'view-project-templet',
					id: 'view-project-templet',
					renderer: () => w(ViewProjectTempletContainer, {}),
				}),
				w(Outlet, {
					key: 'view-project-service',
					id: 'view-project-service',
					renderer: () => w(ViewProjectServiceContainer, {}),
				}),
				w(Outlet, { key: 'new-page-root', id: 'new-page-root', renderer: () => w(NewPageContainer, {}) }),
				w(Outlet, { key: 'new-group-root', id: 'new-group-root', renderer: () => w(NewGroupContainer, {}) }),
				w(Outlet, { key: 'new-page', id: 'new-page', renderer: () => w(NewPageContainer, {}) }),
				w(Outlet, { key: 'new-group', id: 'new-group', renderer: () => w(NewGroupContainer, {}) }),

				w(Outlet, { key: 'list-release', id: 'list-release', renderer: () => w(ListReleaseContainer, {}) }),
				w(Outlet, {
					key: 'new-release',
					id: 'new-release',
					renderer: () => w(NewReleaseContainer, {}),
				}),
				w(Outlet, { key: 'view-release', id: 'view-release', renderer: () => w(ViewReleaseContainer, {}) }),
				w(Outlet, { key: 'docs', id: 'docs', renderer: () => w(ViewDocumentContainer, {}) }),
				w(Outlet, {
					key: 'list-component-repo',
					id: 'list-component-repo',
					renderer: () => w(ListComponentRepoContainer, {}),
				}),
				// 登录用户-setting
				w(Outlet, {
					key: 'settings-profile',
					id: 'settings-profile',
					renderer: () => w(ProfileContainer, {}),
				}),
				w(Outlet, {
					key: 'settings-marketplace',
					id: 'settings-marketplace',
					renderer: () => w(ListMyComponentRepoContainer, {}),
				}),
				w(Outlet, {
					key: 'view-component-repo-publish-task',
					id: 'view-component-repo-publish-task',
					renderer: () => w(ViewComponentRepoPublishTaskContainer, {}),
				}),
				w(Outlet, { key: 'about', id: 'about', renderer: () => w(About, {}) }),
				w(Outlet, {
					id: 'errorOutlet',
					renderer: () => {
						return w(Exception, { type: '404' });
					},
				}),
			]),
			w(FooterContainer, {}),
		]);
	}
}
