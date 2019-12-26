<template>
  <v-card>
    <v-card-title>
      <!--<v-btn color="primary" @click="addOrder">创建订单</v-btn>-->
      <!--搜索框，与search属性关联-->
      <v-spacer/>
      <v-flex xs3>
        <v-text-field label="输入关键字搜索" v-model.lazy="search" append-icon="search" hide-details/>
      </v-flex>
    </v-card-title>
    <v-divider/>
    <v-data-table

    >
      <template slot="items" slot-scope="props">

      </template>
    </v-data-table>

  </v-card>
</template>

<script>
  export default {
    name: "statistics"
  }
</script>

<style scoped>

</style>
