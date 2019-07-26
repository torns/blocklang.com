import * as css from './ViewProject.m.css';
import ThemedMixin, { theme } from '@dojo/framework/widget-core/mixins/Themed';
import I18nMixin from '@dojo/framework/widget-core/mixins/I18n';
import WidgetBase from '@dojo/framework/widget-core/WidgetBase';
import { v } from '@dojo/framework/widget-core/d';
import * as c from '../../className';

export interface ViewProjectPageProperties {}

@theme(css)
export default class ViewProjectPage extends ThemedMixin(I18nMixin(WidgetBase))<ViewProjectPageProperties> {
	//private _localizedMessages = this.localizeBundle(messageBundle);

	protected render() {
		return v('div', { classes: [c.text_center, c.mt_5] }, [v('h1', ['项目 - 页面']), v('p', ['未开发'])]);
	}
}
