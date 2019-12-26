<template>
  <v-container fluid grid-list-md>


    <el-date-picker
      v-model="dateRange"
      type="daterange"
      range-separator="至"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      @change="fetchData()">
    </el-date-picker>

    <!-- 为ECharts准备一个具备大小（宽高）的Dom -->
    <div id="main" style="width: 800px;height:600px;"></div>

    <el-table :data="tableData" border style="width: 100%">
      <el-table-column
        prop="categoryName"
        label="一级分类"
        width="200"/>
      <el-table-column
        prop="num"
        label="销售数量"
        width="200"/>
      <el-table-column
        label="数量比例"
        width="200">
        <template slot-scope="scope">
          {{(scope.row.num/totalNum*100).toFixed(0)}}%
        </template>
      </el-table-column>
      <el-table-column
        prop="money"
        label="销售额"
        width="200">
        <template slot-scope="scope">
          {{(scope.row.money/100).toFixed(2)}}
        </template>
      </el-table-column>
      <el-table-column
        label="金额比例"
        width="200">
        <template slot-scope="scope">
          {{(scope.row.money/totalMoney*100).toFixed(4)}}%
        </template>
      </el-table-column>

    </el-table>
  </v-container>

</template>


<script>
  import '../util'
  // 引入 ECharts 主模块
  var echarts = require('echarts/lib/echarts');
  require('echarts/lib/chart/bar');
  require('echarts/lib/chart/pie');

  export default {
    name: "dashboard",
    data() {
      return {
        tableData: [],
        dateRange: [],
        totalNum: 0,
        totalMoney: 0
      }
    },
    methods: {
      fetchData() {
        let date1 = this.dateRange[0].Format("yyyy-MM-dd");
        //console.log(date1);
        let date2 = this.dateRange[1].Format("yyyy-MM-dd");
        this.$http.get("/item/categoryReport/category1Count", {
          params: {
            date1: date1,
            date2: date2
          }
        }).then(resp => {
          console.log(resp.data);
          this.tableData = resp.data;

          //计算总销量和总金额
          this.totalNum = 0;
          this.totalMoney = 0;
          //数据表
          for (let i = 0; i < this.tableData.length; i++) {
            this.totalNum += this.tableData[i].num;

            //console.log("1",this.totalNum);
            this.totalMoney += this.tableData[i].money;
            //console.log(this.totalMoney);
          }
          console.log("1",this.totalNum);
          console.log("2",this.totalMoney);
          //图例数据
          let legendData = [];
          let numData = [];
          let moneyData = [];
          for(let i=0;i<this.tableData.length;i++){
            legendData.push(this.tableData[i].categoryName);
            numData.push( { name: this.tableData[i].categoryName ,value: this.tableData[i].num  } );
            moneyData.push( { name: this.tableData[i].categoryName ,value: this.tableData[i].money  } );
          }

          let myChart = echarts.init(document.getElementById('main'));
          //生成统计图
          // 指定图表的配置项和数据    a
          let option = {
            title: {
              text: '商品类目销售统计',
              subtext: '一级类目',
              x: 'center'
            },
            tooltip: {
              trigger: 'item',
              formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            legend: {
              type: 'scroll',
              orient: 'vertical',
              right: 10,
              top: 20,
              bottom: 20,
              data: legendData
            },
            series: [
              {
                name: '销售额',
                type: 'pie',
                radius: '35%',
                center: ['30%', '50%'],
                data: numData,
                itemStyle: {
                  emphasis: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                }
              },
              {
                name: '销售量',
                type: 'pie',
                radius: '35%',
                center: ['70%', '50%'],
                data: moneyData,
                itemStyle: {
                  emphasis: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                }
              }
            ]
          };
          // 使用刚指定的配置项和数据显示图表。
          myChart.setOption(option);
          //myChart.on("click",function (params) {
          //    alert(params.name)
          //})

        })


      }
    },
    mounted() {

    }

  }
</script>

<style scoped>

</style>
