package com.eay.cloud.core.search.restclient;

import com.easy.cloud.core.common.json.utils.EcJSONUtils;
import com.easy.cloud.core.common.log.utils.EcLogUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author daiqi
 * @create 2018-11-07 14:28
 */

public class RestClientTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    RestHighLevelClient client = null;

    @Before
    public void init() {
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
    }

    @After
    public void destroy() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void testIndexJsonStr() {
        IndexRequest request = new IndexRequest("posts", "_doc", "1");
        String jsonString = EcJSONUtils.toJSONString(new Person("zhangsan", new Date(), 22, "HelloWorld2"));
        request.source(jsonString, XContentType.JSON);
        try {
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            EcLogUtils.info("index的响应信息", response, logger);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                logger.info("版本异常");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdate() {
        UpdateRequest request = new UpdateRequest("posts", "_doc", "1");
        try {
            String json = EcJSONUtils.toJSONString(new Person("zhangsan", new Date(), 22, "HelloWorld2"));
            request.doc(json, XContentType.JSON);
            UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

            EcLogUtils.info("update的响应信息", response, logger);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGet() {
        GetRequest getRequest = new GetRequest("posts", "_doc", "1");
        try {
//            String [] includes = new String[]{"message"};
//            String [] excludes = Strings.EMPTY_ARRAY;
//
//            FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
//            getRequest.fetchSourceContext(fetchSourceContext);
//            getRequest.version(1L);
            // 设置source不可用
//            getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
//            getRequest.storedFields("message");
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
            EcLogUtils.info("响应的结果", response.getSource(), logger);
        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getAsync() throws InterruptedException {
        GetRequest getRequest = new GetRequest("posts", "_doc", "1");
        try {
            String[] includes = new String[]{"message"};
            String[] excludes = Strings.EMPTY_ARRAY;

            FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
            getRequest.fetchSourceContext(fetchSourceContext);
            // 设置source不可用
//            getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
            client.getAsync(getRequest, RequestOptions.DEFAULT, new ActionListener<GetResponse>() {
                @Override
                public void onResponse(GetResponse documentFields) {
                    EcLogUtils.info("响应的结果", documentFields.getSource().get("message"), logger);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });

        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
        }
        Thread.sleep(10000L);
    }

    @Test
    public void testDelete() {
        DeleteRequest deleteRequest = new DeleteRequest("posts", "_doc", "1");
        try {
            DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);

            EcLogUtils.info("删除", deleteResponse, logger);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testBulkRequest() {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new IndexRequest("posts", "_doc", "5").source(XContentType.JSON, "field", "foo"));
        bulkRequest.add(new IndexRequest("posts", "_doc", "6").source(XContentType.JSON, "field", "bar"));
        bulkRequest.add(new IndexRequest("posts", "_doc", "7").source(XContentType.JSON, "field", "bat"));
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            EcLogUtils.info("批量插入", bulkResponse, logger);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    BulkProcessor.Listener listener = new BulkProcessor.Listener() {
        @Override
        public void beforeBulk(long executionId, BulkRequest request) {

        }

        @Override
        public void afterBulk(long executionId, BulkRequest request,
                              BulkResponse response) {

        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

        }
    };

    @Test
    public void testBulkProcessor() throws InterruptedException {

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
        BulkProcessor.Builder builder = BulkProcessor.builder(bulkConsumer, listener);
        builder.setBulkActions(500);
        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        builder.setConcurrentRequests(0);
        builder.setFlushInterval(TimeValue.timeValueSeconds(10L));
        builder.setBackoffPolicy(BackoffPolicy
                .constantBackoff(TimeValue.timeValueSeconds(1L), 3));
        BulkProcessor bulkProcessor = builder.build();
        IndexRequest one = new IndexRequest("posts", "doc", "1").
                source(XContentType.JSON, "title",
                        "In which order are my Elasticsearch queries executed?");
        IndexRequest two = new IndexRequest("posts", "doc", "2")
                .source(XContentType.JSON, "title",
                        "Current status and upcoming changes in Elasticsearch");
        IndexRequest three = new IndexRequest("posts", "doc", "3")
                .source(XContentType.JSON, "title",
                        "The Future of Federated Search in Elasticsearch");

        bulkProcessor.add(one);
        bulkProcessor.add(two);
        bulkProcessor.add(three);

        bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
    }

    @Test
    public void testQuery() {
        SearchRequest request = new SearchRequest("posts");
        request.types("_doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("zhangs", "userName", "message")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);

        searchSourceBuilder.query(matchQueryBuilder);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle =
                new HighlightBuilder.Field("message");
        highlightTitle.highlighterType("unified");
        highlightBuilder.field(highlightTitle);
        HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("userName");
        highlightBuilder.field(highlightUser);
        searchSourceBuilder.highlighter(highlightBuilder);
//        searchSourceBuilder.query(QueryBuilders.termQuery("userName", "zhangsan"));
        request.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits.getHits()) {
                EcLogUtils.info("查询响应", hit.getSourceAsMap(), logger);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField highlight = highlightFields.get("userName");
                if (highlight != null) {
                    Text[] fragments = highlight.fragments();
                    String fragmentString = fragments[0].string();
                    EcLogUtils.info("高亮的值", fragmentString, logger);
                }

            }
//            SearchHit[] searchHits = searchResponse.getHits().getHits();
//            for (SearchHit hit : searchHits) {
//                EcLogUtils.info("查询响应", hit.getSourceAsMap(), logger);
//                Aggregations aggregations = searchResponse.getAggregations();
//                Terms byCompanyAggregation = aggregations.get("ages");
//                MultiBucketsAggregation.Bucket elasticBucket = byCompanyAggregation.getBucketByKey("Elastic");
//                Avg averageAge = elasticBucket.getAggregations().get("average_age");
//                double avg = averageAge.getValue();
//                EcLogUtils.info("高亮的值", avg, logger);
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public class Person {
        private String userName;
        private Date postDate;
        private Integer age;
        private String message;

        public Person() {

        }

        public Person(String userName, Date postDate, Integer age, String message) {
            this.userName = userName;
            this.postDate = postDate;
            this.age = age;
            this.message = message;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Date getPostDate() {
            return postDate;
        }

        public void setPostDate(Date postDate) {
            this.postDate = postDate;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
