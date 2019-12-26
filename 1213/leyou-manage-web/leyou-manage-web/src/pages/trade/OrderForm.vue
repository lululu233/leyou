<template>


  <v-form>
    <v-text-field v-model="order.orderId" label="订单ID" required/>
    <v-select
      :items="items"
      item-text="text"
      item-value="value"
      label="选择修改的订单状态号"
      v-model="select"
    >
    </v-select>
    <v-layout class="my-4" row>
      <v-spacer/>
      <v-btn @click="submit" color="primary">提交</v-btn>
      <v-btn @click="cancel">取消</v-btn>
    </v-layout>
  </v-form>


</template>

<script>
  export default {
    name: "order-form",
    props:{
      oldOrder:{
        type:Object
      }
    },
    data: () => ({
      order:{},
      select:{},
      test:{text:"未付款",value: 1},
      items: [{text:"1-未付款",value: 1},{text:"2-已付款未发货",value: 2},{text:"3-已发货未确认",value: 3},{text:"4-已确认未评价",value: 4},{text:"5-交易关闭",value: 5},{text:"6-交易成功",value: 6}],

    }),
    methods:{
      submit(){
        this.order.status = this.select;
        this.$http.put("/order/order/updateStatus/" + this.order.orderId + "/" + this.order.status
        ).then(resp => {
          //弹窗提示
          this.$emit("close");
          this.$message.success("更新成功");
        })

      },
      cancel(){

      }
    },
    watch:{
      oldOrder:{
        handler(val){
          if(val){
            this.order = Object.deepCopy(val);
            if(this.order.status === 1){
              this.select = {text:"1 未付款",value: 1}
            }else if(this.order.status === 2){
              this.select = {text:"2 已付款未发货",value: 2}
            }else if(this.order.status === 3){
              this.select = {text:"3 已发货未确认",value: 3}
            }else if(this.order.status === 4){
              this.select = {text:"4 已确认未评价",value: 4}
            }else if(this.order.status === 5){
              this.select = {text:"5 交易关闭",value: 5}
            }else if(this.order.status === 6){
              this.select = {text:"6 交易成功，已评价",value: 6}
            }
          }else {
            this.order = {
              orderId : '',
              status: ''
            }
          }
        },
        deep: true
      }
    }
  }
</script>
