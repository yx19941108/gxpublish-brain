# 节点扩展属性

::: tip
- 当业务系统需要给节点添加扩展属性时，可以按照这个进行扩展。

:::

## 1、实现节点扩展属性数据模型接口

演示项目`hh-vue`中实现类[NodeExtServiceImpl](https://gitee.com/min290/hh-vue.git)

```java
public interface NodeExtService {

    /**
     * 获取节点扩展属性
     * @return 结果
     */
    List<NodeExt> getNodeExt();
}
```

## 2、扩展属性数据模型规则
::: code-tabs#shell

@tab:active 模型规则

```shell
[
  {
    "code": "base",                         -- 编码，此json中唯一
    "name": "按钮权限",		                -- 名称，如果type为新页签时，作为页签名称
    "desc": "基础设置扩展属性",                -- 描述
    "type": 1,                              -- 1：基础设置 2：新页签
    "childs": [                             -- 子集
      {
        "code": "base1",                -- 编码，此json中唯一
        "label": "输入框",               -- label名称
        "desc": "基础设置扩展属性1",       -- 描述
        "type": 1,                      -- 1：输入框 2：文本域 3：下拉框 4：选择框
        "must": true,                   -- 是否必填
        "multiple": true,               -- 是否多选
        "dict": [                       -- 字典，下拉框和复选框时用到
          {
            "label": "选项A",        -- 选项label
            "selected": true,       -- 是否默认选中
            "value": "1"             -- 选项值"
          },
          {
            "label": "选项B",
            "value": "2"
          }
        ]
      }
    ]
  }
]

```

@tab 模型示例

```json
[
	{
		"code": "base",
		"name": null,
		"desc": "基础设置扩展属性",
		"type": 1,
		"childs": [
			{
				"code": "base1",
				"desc": "基础设置扩展属性1",
				"label": "输入框",
				"type": 1,
				"must": true,
				"multiple": false,
				"dict": null
			},
			{
				"code": "base2",
				"desc": "基础设置扩展属性2",
				"label": "文本域",
				"type": 2,
				"must": false,
				"multiple": false,
				"dict": null
			},
			{
				"code": "base3",
				"desc": "基础设置扩展属性3",
				"label": "下一步角色",
				"type": 3,
				"must": false,
				"multiple": false,
				"dict": [
					{
						"label": "普通角色",
						"value": "common",
						"selected": true
					},
					{
						"label": "领导",
						"value": "leader",
						"selected": false
					},
					{
						"label": "员工",
						"value": "yuangong",
						"selected": false
					}
				]
			},
			{
				"code": "base4",
				"desc": "基础设置扩展属性4",
				"label": "单选框",
				"type": 4,
				"must": false,
				"multiple": false,
				"dict": [
					{
						"label": "是否弹窗选人",
						"value": "1",
						"selected": true
					},
					{
						"label": "是否能委托",
						"value": "2",
						"selected": true
					},
					{
						"label": "是否能转办",
						"value": "3",
						"selected": false
					},
					{
						"label": "是否能抄送",
						"value": "4",
						"selected": false
					},
					{
						"label": "是否显示退回",
						"value": "5",
						"selected": false
					},
					{
						"label": "是否能加签",
						"value": "6",
						"selected": false
					},
					{
						"label": "是否能减签",
						"value": "7",
						"selected": false
					}
				]
			}
		]
	},
	{
		"code": "new_tabs",
		"name": "按钮权限",
		"desc": "按钮权限设置",
		"type": 2,
		"childs": [
			{
				"code": "new_tabs1",
				"desc": "按钮权限1",
				"label": "复选框",
				"type": 4,
				"must": false,
				"multiple": true,
				"dict": [
					{
						"label": "是否弹窗选人",
						"value": "1",
						"selected": false
					},
					{
						"label": "是否能委托",
						"value": "2",
						"selected": false
					},
					{
						"label": "是否能转办",
						"value": "3",
						"selected": true
					},
					{
						"label": "是否能抄送",
						"value": "4",
						"selected": true
					},
					{
						"label": "是否显示退回",
						"value": "5",
						"selected": false
					},
					{
						"label": "是否能加签",
						"value": "6",
						"selected": false
					},
					{
						"label": "是否能减签",
						"value": "7",
						"selected": false
					}
				]
			},
			{
				"code": "new_tabs2",
				"desc": "基础设置扩展属性3",
				"label": "下拉选-多选",
				"type": 3,
				"must": false,
				"multiple": true,
				"dict": [
					{
						"label": "选项A",
						"value": "1",
						"selected": true
					},
					{
						"label": "选项B",
						"value": "2",
						"selected": false
					},
					{
						"label": "选项C",
						"value": "3",
						"selected": false
					}
				]
			}
		]
	}
]
```

:::




## 3、设计器渲染效果
<div><img src="https://foruda.gitee.com/images/1740388220090328621/a87819cd_2218307.png" width="800"></div>
<div><img src="https://foruda.gitee.com/images/1740388227772388546/b759aab9_2218307.png" width="800"></div>

## 4、扩展属性入库
点击保存后，保存到节点表的`ext`字段，格式如下：

```

[
  {
    "code": "base1",       -- 编码
    "value": "1"           -- 值，如果复选框，多个值，用逗号拼接
  },
  {
    "code": "btn_auth1",     -- 编码
    "value": "1,2"           -- 值，如果复选框，多个值，用逗号拼接
  }
]

```

## 5、扩展属性使用
业务系统自行查询，按照自己的需求，自行实现
