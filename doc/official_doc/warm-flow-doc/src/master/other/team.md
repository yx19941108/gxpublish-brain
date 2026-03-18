# 团队


<table>
    <thead>
        <tr style="text-align: left;">
            <th>avatar</th>
            <th>name</th>
            <th>email</th>
            <th>role</th>
            <th>contributions</th>
        </tr>
    </thead>
    <tbody>
        <tr v-for="(item, index) in authorsList" :key="index">
            <td><img src="/logo.png" width="30px"></td>
            <td>{{ item.name }}</td>
            <td><a :href="'mailto:' + item.email">{{ item.email }}</a></td>
            <td>{{ item.role }}</td>
            <td>{{ item.contributions }}</td>
        </tr>
    </tbody>
</table>

<script>
import { ref, onMounted } from 'vue';
 
export default {
  setup() {
    const authorsList = ref([]);
 
    const fetchData = async () => {
      try {
        const response = await fetch('https://gitee.com/api/v5/repos/dromara/warm-flow/contributors?type=authors');
        authorsList.value = await response.json();
        authorsList.value = authorsList.value.map(author => {
          if (author.name === 'warm') {
            return { ...author, role: 'Author' };
          } else if (['xiarigang', 'vanlin', 'liangli', 'Zhen'].includes(author.name)) {
            return { ...author, role: 'PMC' };
          } else {
            return { ...author, role: 'Committer' };
          }
        });
      } catch (error) {
        console.error('Error fetching data:', error);
      }
    };
 
    onMounted(fetchData);
 
    return {
      authorsList,
    };
  },
};
</script>

<style>
.header {
  margin: calc(2rem - 0.165em) 0em 1rem;
}
.user-list-item {
  display: inline-flex;
  align-items: center;
  margin: 15px 0;
  padding: 14px 0 14px 10px;
  width: 178px;
  .avatar {
    height: 50px;
    margin-right: 10px;
    border-radius: 50%;
  }
  .content {
    line-height: 22px;
    .username,
    .sub-info {
      display: block;
      color: #40485b;
      font-size: 14px;
      font-weight: 400;
    }
    .username {
      font-weight: 600;
      font-size: 16px;
    }
  }
}
</style>

