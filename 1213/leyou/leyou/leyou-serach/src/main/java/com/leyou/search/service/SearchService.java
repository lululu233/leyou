package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    //使用json 工具
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();

        //根据分类ID查询分类名称
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //根据品牌id 查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据spu id 查询 所有sku
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        //初始化价格 收集所有sku 价格
        ArrayList<Long> prices = new ArrayList<>();

        //收集 sku(实际项目会很上百个属性) 的必要字段信息 Map<id/title/price/image, Object>
        List<Map<String, Object>> skuMapList = new ArrayList<>();

        skus.forEach(sku -> {
            prices.add(sku.getPrice());

            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            //获取 sku中的 数据图片 可能 为多张 是以 ，分割 StringUtils.split(sku.getImages(), ",") 返回的是数组 则取第一张图片
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);

            skuMapList.add(map);

        });

        //查询tb_spec_param规格参数 key Map<key,value>  根据 cid3 76 手机 是否查询字段 searching
        List<SpecParam> params = this.specificationClient.queryParmas(null, spu.getCid3(), null, true);

        //根据spuId查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        //json 反向序列化 为对象  把 tb_spu_detail 中的 generic_spec json 数据 反序列化为 map集合
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        //special_spec (特殊规格参数)反序列化
        Map<String, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });

        //搜索字段名从tb_spec_param 获取 值 从 tb_spu_detail 获取或选择 字段 map
        Map<String, Object> specs = new HashMap<>();
        params.forEach(param -> {
            //判断 是否为通用规格参数
            if (param.getGeneric()) {
                String value = genericSpecMap.get(param.getId().toString()).toString();

                //判断是否为数值类型 则返回 范围区间
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);

                }
                specs.put(param.getName(), value);

            } else {
                //特殊参数
                List<Object> value = specialSpecMap.get(param.getId().toString());
                specs.put(param.getName(), value);
            }
        });

        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        //拼接all字段 需要分类名称以及品牌名称
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " ") + " " + brand);//加空格 防止不合适的分词产生

        //获取spu 下的所有sku 价格
        goods.setPrice(prices);

        //获取所有spu下的所有sku 并转成 序列化writeValueAsString  json
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));

        //获取所有查询 的规格参数tb_spec_param 中 searching 为1  为Map {name:value}--》{显示搜索分类:具体值}
        goods.setSpecs(specs);

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            // 如果用户没搜索条件，我们可以给默认的，或者返回null
            return null;
        }

        Integer page = request.getPage() - 1;// page 从0开始
        Integer size = request.getSize();

        // 1、创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2、查询
        // 2.1、对结果进行筛选
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        // 2.2、基本查询
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", key);
        //添加bool查询
        BoolQueryBuilder basicQuery = buildBoolQueryBuilder(request);
        queryBuilder.withQuery(basicQuery);

        // 2.3、分页
        queryBuilder.withPageable(PageRequest.of(page, size));

        //添加分类和品牌的聚合
        String categoriesAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoriesAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 3、返回 普通结果集 和聚合结果集合
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        //获取聚合结果集并解析
        //分类
        List<Map<String, Object>> categories = getCategoryAggResult(goodsPage.getAggregation(categoriesAggName));
        //品牌
        List<Brand> brands = getBrandAggResult(goodsPage.getAggregation(brandAggName));
        //规格参数
        //判断是否是一个分类 只有一个分类才做规格参数聚合
        List<Map<String, Object>> specs = null;
        if(!CollectionUtils.isEmpty(categories) && categories.size() ==1){
            //对规格参数聚合
            specs = getParamAggResult((Long)categories.get(0).get("id"), basicQuery);

        }


        // 4、解析结果
        long total = goodsPage.getTotalElements();
        long totalPage = (total + size - 1) / size;
        return new SearchResult(total, totalPage, goodsPage.getContent(), categories, brands, specs);
    }

    /**
     * 构建 bool 查询 组合查询
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //给 bool查询添加 基本查询条件 Operator.AND 分词 and
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //获取用户选择过滤信息
        Map<String, Object> filter = request.getFilter();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.equals("品牌",key)){
                key = "brandId";
            }else if (StringUtils.equals("分类", key)){
                key = "cid3";
            }else {
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }

        return boolQueryBuilder;
    }


    /**
     * 根据查询条件聚合规格参数
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String,Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
        //自定义查询对象构建
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本查询条件
        queryBuilder.withQuery(basicQuery);
        //查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParmas(null, cid, null, true);

        //根据 获取的规格参数名添加规格参数的集合
        params.forEach(param ->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
        });

        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        //执行集合的查询 获取聚合结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        List<Map<String,Object>> specs = new ArrayList<>();
        //解析聚合结果集map  <聚合名，集合对象>
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            //map<规格参数名k,聚合规格参数值options>
            Map<String,Object> map = new HashMap<>();
            map.put("k", entry.getKey());

            //初始化一个option集合 收集 桶中的key
            List<String> options = new ArrayList<>();

            //获取聚合
            StringTerms terms = (StringTerms) entry.getValue();

            //获取桶集合 把桶中的 key 组成数组
            terms.getBuckets().forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });

            map.put("options", options);
            specs.add(map);
        }


        return specs;
    }

    /**
     * 解析品牌聚合结果集
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms terms = (LongTerms)aggregation;

        //收集 brand id 数组
        //List<Brand> brands = new ArrayList<>();
        //处理 旧数组封装成新数组  获取 桶 id 并查询 brand 并封装成新数组
        return terms.getBuckets().stream().map(bucket -> {
            return this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());

        /*//获取 聚合里的桶
        terms.getBuckets().forEach(bucket -> {
            Brand brand = this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });*/


    }

    /**
     * 解析分类聚合结果集
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms = (LongTerms)aggregation;
        //return 返回 方法 定义返回的类型
        //获取 桶集合 转化成List<Map<，>>
        return terms.getBuckets().stream().map(bucket -> {
            //初始化map
            Map<String,Object> map = new HashMap<>();
            //获取桶中分类 id 查询分类名称
            long id = bucket.getKeyAsNumber().longValue();
            //分类查询id查询 分类名称
            List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(id));
            map.put("id",id);
            map.put("name", names.get(0));
            return map;
        }).collect(Collectors.toList());

    }

    public void save(Long id) throws IOException {

        Spu spu = this.goodsClient.querySpuById(id);
        Goods goods = this.buildGoods(spu);
        this.goodsRepository.save(goods);
    }

    public void delete(Long id) {

        this.goodsRepository.deleteById(id);
    }
}
