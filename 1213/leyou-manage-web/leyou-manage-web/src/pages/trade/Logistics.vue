<template>
  <v-card>
    <v-card-title>
      <!--搜索框，与search属性关联-->
      <v-spacer/>
      <v-flex xs3>
        <v-text-field label="输入订单状态号" v-model.lazy="search" append-icon="search" hide-details/>
      </v-flex>
    </v-card-title>
    <v-divider/>
    <v-data-table
      :headers="headers"
      :items="orders"
      :pagination.sync="pagination"
      :total-items="totalOrders"
      :loading="loading"
      class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <div style="display: none"><input type="text" vaule="props.item" v-model="subOrder"></div>
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.orderId }}</td>
        <td class="text-xs-center">{{props.item.createTime}}</td>
        <td class="text-xs-center">{{props.item.receiverAddress}}</td>
        <td class="text-xs-center">{{props.item.shippingAddress}}</td>
        <td class="justify-center layout px-0">
          <v-btn icon @click="editOrder(props.item)">
            <i class="el-icon-edit"/>
          </v-btn>
          <v-btn icon @click="deleteOrder(props.item)">
            <i class="el-icon-delete"/>
          </v-btn>
        </td>
      </template>
    </v-data-table>

  </v-card>
</template>

<script>
  // 导入自定义的表单组件


  export default {
    name: "logistics",
    status: null,
    data() {
      return {
        logisticsId:"",
        subOrder:{},
        select:{},
        logisticsList:{},
        search: '', // 搜索过滤字段
        totalOrders: 0, // 总条数
        orders: [], // 当前页订单数据
        order: [],
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: '物流编号', align: 'center', value: 'id'},
          {text: '订单编号', align: 'center', sortable: false, value: 'order_id'},
          {text: '创建时间', align: 'center', sortable: false, value: 'create_time'},
          {text: '发货地址', align: 'center', value: 'shipping_address', sortable: true,},
          {text: '送货地址', align: 'center', value: 'receiver_address', sortable: false},
          {text: '操作', align: 'center', value: 'logisticsId', sortable: false},

        ],
        show: false,// 控制对话框的显示
        oldOrder: {}, // 即将被编辑的品牌数据
        isEdit: false, // 是否是编辑
      }
    },
    mounted() { // 渲染后执行
      // 查询数据
      this.selectLogistics();
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
      selectLogistics(){
        this.$http.get("/order/order/selectLogistics")
          .then(resp => { // 这里使用箭头函数
            this.logisticsList = resp.data;
        });
      },
      getDataFromServer() { // 从服务的加载数的方法。
        // 发起请求
        this.$http.get("/order/logistics/queryLogistics", {
          params: {
            key: this.search, // 搜索条件 做成下拉框
            page: this.pagination.page,// 当前页
            rows: this.pagination.rowsPerPage,// 每页大小


          }
        },{
          transformResponse: [
            function (data) {/*直接返回防止进度损失*/
              return data;
            }
          ]
        }).then(resp => { // 这里使用箭头函数
          //console.log(resp);
          this.orders = resp.data.items;
          //console.log(this.orders);
          this.totalOrders = resp.data.total;

          // 完成赋值后，把加载状态赋值为false
          this.loading = false;
        })
      },
      deliverGoods(orderId){
        this.$http.get("/order/order/createLogistics", {
          params: {
            orderId: orderId,// 提交订单
            select: this.select,// 物流商id
            logisticsId: this.logisticsId //物流单号   sad
          }
        }).then(resp => { // 这里使用箭头函数

        })
      }

      },
      async editOrder(oldOrder){//异步 变同步
        // 根据品牌信息查询商品分类

        // 修改标记
        this.isEdit = true;
        // 控制弹窗可见：
        this.show = true;
        // 获取要编辑的goods
        this.oldOrder = oldOrder;
      },
      closeWindow(){
        // 重新加载数据
        this.getDataFromServer();
        // 关闭窗口
        this.show = false;
      }

  }
</script>

<style scoped>

</style>
