# `PAGE_FUNC_NODE` - 可视化函数设计器中的节点

在可视化的函数设计器中，通过节点、端口和连接线来描述函数定义、函数调用顺序以及数据传输关系。

1. 函数节点表示函数定义或函数调用
2. 函数节点中的序列端口描述函数的调用顺序
3. 函数节点中的数据端口描述函数的输入参数和返回值的传递关系

页面中显示的引用自组件或部件中的文本，要支持组件版本升级。

## 字段

| 字段名              | 注释                        | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | --------------------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键                        | varchar | 32   |        | 是   | 否   |
| project_resource_id | 项目资源标识                | int     |      |        |      | 否   |
| page_func_id        | 页面函数标识                | varchar | 32   |        |      | 否   |
| left                | 相对于设计器左上角的 x 坐标 | int     |      |        |      | 否   |
| top                 | 相对于设计器左上角的 y 坐标 | int     |      |        |      | 否   |
| layout              | 节点布局                    | varchar | 16   |        |      | 否   |
| category            | 定义或调用的函数分类        | varchar | 16   |        |      | 否   |
| data_item_id        | 页面数据项标识              | varchar | 32   |        |      | 是   |

| bind_source  | 节点绑定的数据源            | varchar | 16   |        |      | 是   |
| api_repo_id  | API 仓库标识                | int     |      |        |      | 是   |
| code         | 组件编码                    | varchar | 32   |        |      | 是   |

TODO: 新增一个 dataId 字段，不要将引用的页面数据存到 code 中？

## 约束

* 主键：`PK_PAGE_FUNC_NODE`
* 外键：(*未设置*)`FK_PAGE_FUNC_NODE_ON_FUNC_ID`，`page_func_id` 对应 `PAGE_FUNC` 表的 `dbid`
* 索引：`IDX_PAGE_FUNC_NODE_ON_PROJECT_RESOURCE_ID`(普通索引)，对应字段 `project_resource_id`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `project_resource_id` 是一个冗余字段，便于快速查找出一个页面中所有事件处理函数的节点
3. `layout` 字段与节点在可视化设计器中的布局有关：`flowControl` 表示使用流程控制的节点布局，`data` 表示使用数据的节点布局
4. `category` 的值为：`function` 表示函数定义，`functionCall` 表示函数调用，`variableSet` 表示为变量设置值，`variableGet` 表示获取变量的值
5. 如果 `category` 的值为 `function`，则 `bind_source`、`api_repo_id` 和 `code` 三个字段的值为空
6. `data_item_id` 引用的是 `page_data` 中的 `dbid`，当 `category` 的值为 `variableSet` 或 `variableGet` 时需设置此值
7. `bind_source` 表示 `api_repo_id` 和 `code` 的取值来源，值为：`data` 表示取自页面数据，`service` 表示取自 RESTful API
8. 如果 `bind_source` 的值为 `data`，则 `api_repo_id` 的值为空，`code` 的值为 `page_data` 中的 `dbid`；如果 `bind_source` 的值为 `service`，则 `api_repo_id` 的值为 `API_REPO` 表中的 `dbid`，`code` 的值为 `API_COMPONENT` 表中的 `code`


另一种设计：

* 如果 `category` 的值为 `function`，则 `bind_source` 的值为 `WidgetEvent`(引用部件事件)、`api_api_id` 的值为 `API_REPO` 表中的 `dbid`，`code` 的值为 `API_COMPONENT` 表中的 `code` 加上 `API_COMPONENT_ATTR` 中的 `code`。但是这些值可以通过 `PAGE_FUNC` 中的 `dbid` 关联出来，所以暂时不用这个设计
