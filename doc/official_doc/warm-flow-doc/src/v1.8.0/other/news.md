# 新闻

<div style="display: flex; justify-content: flex-end; margin-bottom: 10px;">
    <el-input v-model="searchQuery" class="Input" placeholder="搜索标题" @input="filterByTitle" clearable />
    <el-tabs type="border-card" class="Tabs" v-model="activeTab" @tab-change="filterSelect(activeTab)">
      <el-tab-pane label="全部" name=""></el-tab-pane>
      <el-tab-pane v-for="(nt, index) in getType()" :key="nt.key" :label="nt.value" :name="nt.key"></el-tab-pane>
    </el-tabs>
</div>

<table class="no-border" style="width: 100%; border-collapse: collapse; ">
    <tbody>
       <tr v-for="(item, index) in filterList" :key="index" style="font-size: 15px;">
            <td class="no-border" style="width: 10%; text-align: center;">
                <div :style="{ padding: '3px', backgroundColor: getTypeColor(item.type), color: 'white', borderRadius: '4px', margin: '2px' }">
                    {{ getTypeValue(item.type) }}
                </div>
            </td>          
            <td class="no-border" style="padding: 8px; width: 66%">
                <div>
                    <span @click="navigateTo(item.url)" class="link-style">{{ item.title }}</span>
                </div>
            </td>
            <td class="no-border" style="padding: 8px; width: 15%; text-align: right;">{{ item.pushTime }}</td>
        </tr>
    </tbody>
</table>

<script>
import { ref, onMounted } from 'vue';
 
export default {
  setup() {
    const allList = ref([]);
    const filterList = ref([]);
    const newsType = ref( []);
    const selectedType = ref('');
    const searchQuery = ref('');
    const activeTab = ref('');
 
    const fetchData = async () => {
    newsType.value = [
        {
            "key": "news",
            "value": "新闻", 
            "color": "#1890ff"
        },
        {
            "key": "upgrade",
            "value": "升级", 
            "color": "#ff9292"
        },
        {
            "key": "experience",
            "value": "心得", 
            "color": "#71e2a3"
        },
      ];
      filterList.value = allList.value = [
        {
            "type": "upgrade",
            "title": "Warm-Flow 1.8.1 bug修复", 
            "url": "./news/upgrade/Warm-Flow_1.8.1.html",    
            "pushTime": "2025-08-22"
        },
        {
            "type": "upgrade",
            "title": "Warm-Flow重大突破, 自研仿钉钉设计器", 
            "url": "./news/upgrade/Warm-Flow_1.8.0.html",    
            "pushTime": "2025-08-13"
        },
        {
            "type": "upgrade",
            "title": "Warm-Flow发布1.7.6, 设计器重构和流程图优化", 
            "url": "./news/upgrade/12.html",    
            "pushTime": "2025-06-30"
        },
        {
            "type": "upgrade",
            "title": "Warm-Flow发布1.7.4, 设计器和流程图优化", 
            "url": "./news/upgrade/11.html",    
            "pushTime": "2025-06-23"
        },
        {
            "type": "upgrade",
            "title": "Warm-Flow发布1.7.3 端午节（设计器流和流程图大升级）", 
            "url": "./news/upgrade/10.html",    
            "pushTime": "2025-05-13"
        },
        {
            "type": "upgrade",
            "title": "国产免费工作流引擎star 6.5k，Warm-Flow版本升级1.7.2（修复部分缺陷）", 
            "url": "./news/upgrade/9.html",    
            "pushTime": "2025-05-13"
        },
        {
            "type": "upgrade",
            "title": "国产免费工作流引擎star 5.9k，Warm-Flow版本升级1.7.0（新增大量好用功能）", 
            "url": "./news/upgrade/8.html",    
            "pushTime": "2025-04-28"
        },
        {
            "type": "experience",
            "title": "maven如何解决jar包依赖冲突", 
            "url": "./news/experience/7.html",
            "pushTime": "2025-04-16"
        },
        {
            "type": "upgrade",
            "title": "Warm-Flow版本升级1.6.10：修复部分错误", 
            "url": "./news/upgrade/n-9.html",    
            "pushTime": "2025-04-13"
        },
        {
            "type": "news",
            "title": "Warm-Flow过去、现在和未来都不会有商业版", 
            "url": "./news/news/8.html",
            "pushTime": "2025-03-27"
        }, 
        {
            "type": "upgrade",
            "title": "Warm-Flow版本升级1.6.8：修复部分错误", 
            "url": "./news/upgrade/7.html",    
            "pushTime": "2025-03-19"
        },
        {
            "type": "upgrade",
            "title": "三头六臂显神通：Warm-Flow引擎实现多维度灵活配置", 
            "url": "./news/upgrade/6.html",    
            "pushTime": "2025-02-25"
        },
        {
            "type": "upgrade",
            "title": "🧨新春版v1.6.6发布：网关直连和流程图重构，新增Ruoyi-Vue-Plus优秀开源集成案例", 
            "url": "./news/upgrade/4.html",    
            "pushTime": "2025-01-15"
        },
        {
            "type": "experience",
            "title": "WarmFlow工作流动态指定审批人", 
            "url": "./news/experience/6.html",
            "pushTime": "2024-12-06"
        },
        {
            "type": "upgrade",
            "title": "v1.3.4发布：solon集成Dromara Warm-Flow", 
            "url": "./news/upgrade/5.html",    
            "pushTime": "2024-12-04"
        },
        {
            "type": "upgrade",
            "title": "v1.3.1发布：一个自带流程设计器的工作流引擎", 
            "url": "./news/upgrade/3.html",
            "pushTime": "2024-11-01"
        },
        {
            "type": "news", 
            "title": "gitee变成maven私库", 
            "url": "./news/news/2.html",
            "pushTime": "2024-09-29"
        }, 
        {
            "type": "news",
            "title": "工作流引擎Warm-Flow加入Dromara开源社区", 
            "url": "./news/news/1.html",
            "pushTime": "2024-02-22"
        }, 
        {
            "type": "experience",
            "title": "Dromara Warm-Flow工作流引擎数据库主键自增策略实现", 
            "url": "https://juejin.cn/post/7402110528298074152",
            "pushTime": "2024-02-22"
        },
      ]
    };
 
    onMounted(fetchData);
 
    const navigateTo = (url) => {
      window.location.href = url;
    };

    const getType = () => {
      return newsType.value;
    };  

    const getTypeValue = (type) => {
      return newsType.value.find(nt => nt.key === type)?.value || type;
    };
    const getTypeColor = (type) => {
      return newsType.value.find(nt => nt.key === type)?.color || type;
    };

    const filterSelect = (type) => {
      selectedType.value = type;
      if (type) {
        filterList.value = allList.value.filter(item => item.type === type);
      } else {
        fetchData();
      }
      if (searchQuery.value) filterByTitle();
    };

    const getSelected = (type) => {
        return selectedType.value === type
    };

    const filterByTitle = () => {
      if (searchQuery.value) {
        filterList.value = filterList.value.filter(item => 
          item.title.toLowerCase().includes(searchQuery.value.toLowerCase())
        );
      } else {
        filterSelect(selectedType.value);
      }
    };

    return {
      activeTab,
      allList,
      filterList,
      newsType,
      selectedType,
      searchQuery,    
      navigateTo,
      getType,
      getTypeValue,
      getTypeColor,
      filterSelect,
      getSelected,
      filterByTitle,
    };
  },
};
</script>

<style>
.Input {
  width: 180px;
  margin-right: 10px;
}
.Tabs .el-tabs__content {
  display: none;
}
.Tabs .el-tabs__header {
  border-bottom: 0;
}
</style>
