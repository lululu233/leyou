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
        <td class="text-xs-center">{{ props.item.orderId }}</td>
        <td class="text-xs-center">{{props.item.createTime}}</td>
        <td class="text-xs-center">{{ props.item.buyerNick }}</td>
        <td class="text-xs-center">{{ ordersActualPay(props.item.actualPay) }}</td>
        <td class="text-xs-center">{{ props.item.paymentType == '1'? '微信支付' : '货到付款'}}</td>
        <td class="text-xs-center">
          <div class="disabled">{{getStatus(props.item.status)}}</div>
          <li style="list-style-type:none;" v-if="status === 1">未付款</li>
          <li style="list-style-type:none;" v-if="status === 2">已付款未发货</li>
          <li style="list-style-type:none;" v-if="status === 3">已发货未确认</li>
          <li style="list-style-type:none;" v-if="status === 4">已确认未评价</li>
          <li style="list-style-type:none;" v-if="status === 5">交易关闭</li>
          <li style="list-style-type:none;" v-if="status === 6">交易成功，已评价</li>
        </td>
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
    <!--弹出的对话框-->
    <v-dialog max-width="500" v-model="show" persistent scrollable>
      <v-card>
        <!--对话框的标题-->
        <v-toolbar dense dark color="primary">
          <v-toolbar-title>{{isEdit ? '修改' : '新增'}}订单状态</v-toolbar-title>
          <v-spacer/>
          <!--关闭窗口的按钮-->
          <v-btn icon @click="closeWindow"><v-icon>close</v-icon></v-btn>
        </v-toolbar>
        <!--对话框的内容，表单-->
        <v-card-text class="px-5" style="height:400px">
          <order-form @close="closeWindow" :oldOrder="oldOrder" :isEdit="isEdit" ref="orderForm"/>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
  // 导入自定义的表单组件
  import OrderForm from './OrderForm'

  export default {
    name: "order",
    status: null,
    data() {
      return {
        deliverGoods:0,
        search: '', // 搜索过滤字段
        totalOrders: 0, // 总条数
        orders: [], // 当前页订单数据
        order: [],
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: '编号', align: 'center', value: 'id'},
          {text: '提交时间', align: 'center', sortable: false, value: 'create_time'},
          {text: '用户账号', align: 'center', sortable: false, value: 'buyer_nick'},
          {text: '订单金额(实际)', align: 'center', value: 'actual_pay', sortable: true,},
          {text: '支付方式', align: 'center', value: 'payment_type', sortable: false},
          {text: '订单状态', align: 'center', value: 'status', sortable: false},
          {text: '操作', align: 'center', value: 'id', sortable: false},
        ],
        show: false,// 控制对话框的显示
        oldOrder: {}, // 即将被编辑的品牌数据
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
        if(status === 1){
          this.status = 1;
        }else if( status === 2) {
          this.status = 2;
        }else if ( status === 3) {
          this.status = 3;
        }else if ( status === 4) {
          this.status = 4;
        }else if ( status === 5) {
          this.status = 5;
        }else if ( status === 6) {
          this.status = 6;
        }

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
      ordersActualPay(ActualPay){
        return this.$format(ActualPay);
      },
      addOrder() {
        // 修改标记
        this.isEdit = false;
        // 控制弹窗可见：
        this.show = true;
        // 把oldOrder变为null
        this.oldOrder = null;
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
      deleteOrder(order){
        //console.log(order);
        this.$http.get("/order/order/deleteOrder/" + order.orderId,{
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
      closeWindow(){
        // 重新加载数据
        this.getDataFromServer();
        // 关闭窗口
        this.show = false;
      }
    },
    components:{
      OrderForm
    }
  }
</script>

<style scoped>

</style>
