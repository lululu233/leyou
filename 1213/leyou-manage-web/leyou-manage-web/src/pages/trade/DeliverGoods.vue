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
        <td class="text-xs-center">{{ props.item.orderId }}</td>
        <td class="text-xs-center">{{ props.item.buyerNick }}</td>
        <td class="text-xs-center">{{props.item.receiverMobile}}</td>
        <td class="text-xs-center">{{props.item.receiverAddress}}</td>
        <td class="text-xs-center">
          <v-select
            :items="logisticsList"
            item-text="shippingName"
            item-value="shippingCode"
            label="请选择物流"
            v-model="select"
          >
          </v-select>
        </td>
        <td class="text-xs-center">
          <input type="text" value="" v-model="logisticsId" style="border: 1px solid ">
        </td>
        <td class="text-xs-center">
          <v-btn color="primary" @click="deliverGoods(props.item.orderId)">发货</v-btn>
        </td>
      </template>
    </v-data-table>

  </v-card>
</template>

<script>
  // 导入自定义的表单组件


  export default {
    name: "delivergoods",
    status: null,
    data() {
      return {
        logisticsId:"",
        subOrder:{},
        select:{},
        logisticsList:{},
        search: '2', // 搜索过滤字段
        totalOrders: 0, // 总条数
        orders: [], // 当前页订单数据
        order: [],
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: '订单编号', align: 'center', value: 'order_id'},
          {text: '收货人', align: 'center', sortable: false, value: 'buyer_nick'},
          {text: '手机号', align: 'center', sortable: false, value: 'receiver_mobile'},
          {text: '收货地址', align: 'center', value: 'receiver_address', sortable: true,},
          {text: '配送方式', align: 'center', value: 'status', sortable: false},
          {text: '物流单号', align: 'center', value: 'logisticsId', sortable: false},
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
        this.$http.get("/order/order/queryOrders", {
          params: {
            page: this.pagination.page,// 当前页
            rows: this.pagination.rowsPerPage,// 每页大小
            status: this.search, // 搜索条件 做成下拉框

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
