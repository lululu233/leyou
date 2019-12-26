<template>
  <v-card>
    <v-card-title>

      <!--搜索框，与search属性关联-->
      <v-spacer/>
      <v-flex xs3>
        <v-text-field label="输入优惠名" v-model.lazy="search" append-icon="search" hide-details/>
      </v-flex>
    </v-card-title>
    <v-divider/>
    <v-data-table
      :headers="headers"
      :items="preferentials"
      :pagination.sync="pagination"
      :total-items="totalPreferentials"
      :loading="loading"
      class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{props.item.name}}</td>
        <td class="text-xs-center">
          <div class="disabled">{{getStatus(props.item.state)}}</div>
          <li style="list-style-type:none;" v-if="status === 1">活动进行</li>
          <li style="list-style-type:none;" v-if="status === 2">活动结束</li>

        </td>
        <td class="text-xs-center">{{ props.item.startTime }}</td>
        <td class="text-xs-center">{{ props.item.endTime}}</td>
        <td class="text-xs-center">
          <div class="disabled">{{ getStatus(props.item.type)}}</div>
          <li style="list-style-type:none;" v-if="status === 1">翻倍</li>
          <li style="list-style-type:none;" v-if="status === 2">不翻倍</li>
        </td>
        <td class="justify-center layout px-0">
          <v-btn icon @click="editPreferential(props.item)">
            <i class="el-icon-edit"/>
          </v-btn>
          <v-btn icon @click="deletePreferential(props.item)">
            <i class="el-icon-delete"/>
          </v-btn>
        </td>
      </template>
    </v-data-table>
    <!--弹出的对话框-->
    <v-dialog max-width="500" v-model="show" persistent scrollable>
      <v-card>
        <!--对话框的标题-->
        <v-toolbar dense dark color="primary">
          <v-toolbar-title>{{isEdit ? '修改' : '新增'}}订单状态</v-toolbar-title>
          <v-spacer/>
          <!--关闭窗口的按钮-->
          <v-btn icon @click="closeWindow">
            <v-icon>close</v-icon>
          </v-btn>
        </v-toolbar>
        <!--对话框的内容，表单-->
        <v-card-text class="px-5" style="height:400px">
          <promotion-form @close="closeWindow" :oldPreferential="oldPreferential" :isEdit="isEdit"
                             ref="promotionForm"/>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
  import '../../util'
  import PromotionForm from './PromotionForm'

  export default {
    name: "promotion",
    status:'',
    data() {
      return {

        deliverGoods: 0,
        search: '', // 搜索过滤字段
        totalPreferentials: 0, // 总条数
        preferentials: [], // 当前页订单数据
        preferential: [],
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: '编号', align: 'center', value: 'id'},
          {text: '活动名称', align: 'center', sortable: false, value: 'name'},
          {text: '活动状态', align: 'center', sortable: false, value: 'state'},
          {text: '开始时间', align: 'center', value: 'start_time', sortable: true,},
          {text: '结束时间', align: 'center', value: 'end_time', sortable: false},
          {text: '翻倍类型', align: 'center', value: 'type', sortable: false},
          {text: '操作', align: 'center', value: 'id', sortable: false},
        ],
        show: false,// 控制对话框的显示
        oldPreferential: {}, // 即将被编辑的品牌数据
        isEdit: false, // 是否是编辑
      }
    },
    mounted() { // 渲染后执行
      // 查询数据
      this.getDataFromServer();
    },
    watch: {
      pagination: { // 监视pagination属性的变化
        deep: true, // deep为true，会监视pagination的属性及属性中的对象属性变化
        handler() {
          // 变化后的回调函数，这里我们再次调用getDataFromServer即可
          this.getDataFromServer();
        }
      },
      search: { // 监视搜索字段
        handler() {
          this.getDataFromServer();
        }
      }
    },
    methods: {

      getStatus(status) {
        if (status === '1') {
          this.status = 1;
        } else if (status === '2') {
          this.status = 2;
        }
        console.log(this.status);
      },
      getDataFromServer() { // 从服务的加载数的方法。
        // 发起请求
        this.$http.get("/order/preferential/queryPreferentials", {
          params: {
            key: this.search,
            page: this.pagination.page,// 当前页
            rows: this.pagination.rowsPerPage,// 每页大小

          }
        }, {
          transformResponse: [
            function (data) {/*直接返回防止进度损失*/
              return data;
            }
          ]
        }).then(resp => { // 这里使用箭头函数
          //console.log(resp);
          this.preferentials = resp.data.items;
          //console.log(this.preferentials);
          this.totalPreferentials = resp.data.total;

          // 完成赋值后，把加载状态赋值为false
          this.loading = false;
        })
      },
      preferentialsActualPay(ActualPay) {
        return this.$format(ActualPay);
      },
      addPreferential() {
        // 修改标记
        this.isEdit = false;
        // 控制弹窗可见：
        this.show = true;
        // 把oldPreferential变为null
        this.oldPreferential = null;
      },
      async editPreferential(oldPreferential) {//异步 变同步
        // 根据品牌信息查询商品分类

        // 修改标记
        this.isEdit = true;
        // 控制弹窗可见：
        this.show = true;
        // 获取要编辑的goods
        this.oldPreferential = oldPreferential;
      },
      deletePreferential(preferential) {
        //console.log(preferential);
        this.$http.get("/preferential/preferential/deletePreferential/" + preferential.preferentialId, {
          transformResponse: [
            function (data) {/*直接返回防止进度损失*/
              return data;
            }
          ]
        })
          .then(resp => { // 这里使用箭头函数
            this.getDataFromServer();
          })
      },
      closeWindow() {
        // 重新加载数据
        this.getDataFromServer();
        // 关闭窗口
        this.show = false;
      },

    },
    computed:{


    },
    components:{
      PromotionForm
    }

  }
</script>

<style scoped>


</style>
